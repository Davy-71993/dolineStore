package com.example.doline.data

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.concurrent.TimeUnit

// Wire shapes for the "stores"/"profiles" cloud tables, kept separate from the local Room
// entities so the outbox's local JSON snapshot format never has to match the cloud schema.
// Column names here mirror the aliases already used in StoreRepository.fetchStoresFromCloud
// and UserProfileRepository.fetchProfileFromCloud - not verified against a live schema beyond
// that, so double check against the actual Supabase table definitions before relying on this.
@Serializable
private data class StoreCloudPayload(
    val name: String,
    val description: String,
    val address: String? = null,
    val logo: String? = null,
    val status: String? = null,
    @SerialName("keeper_id") val keeperId: String
)

@Serializable
private data class StoreCloudId(val id: Long)

@Serializable
private data class ProfileCloudPayload(
    @SerialName("user_id") val userId: String,
    @SerialName("name") val username: String? = null,
    @SerialName("full_name") val fullNames: String? = null,
    val phone: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val about: String? = null,
    @SerialName("default_address") val defaultAddress: String? = null
)

/**
 * Runs both sync directions for stores/profiles in one pass: first drains [SyncQueueEntity]
 * rows and pushes them to Supabase, in FIFO order per run - a failed row is left in the queue
 * (with its attempt count/error recorded) rather than skipped, to avoid out-of-order writes for
 * that row - then pulls each entity down via [StoreRepository.pull]/[UserProfileRepository.pull].
 * The whole run reports [Result.retry] if anything failed, so WorkManager backs off and retries.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncQueueDao: SyncQueueDao,
    private val storeDao: StoreDao,
    private val storeRepository: StoreRepository,
    private val profileRepository: UserProfileRepository,
    private val supabaseClient: SupabaseClient,
    private val auth: Auth
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val keeperId = auth.currentUserOrNull()?.id ?: return Result.success()

        var hadFailure = false
        for (entry in syncQueueDao.getPending()) {
            try {
                when (entry.entityType) {
                    SyncEntityType.STORE -> pushStore(entry, keeperId)
                    SyncEntityType.PROFILE -> pushProfile(entry)
                }
                syncQueueDao.delete(entry.id)
            } catch (e: Exception) {
                hadFailure = true
                syncQueueDao.markFailed(entry.id, e.message)
            }
        }

        // Pull after pushing, so any of our own just-pushed edits are already on the server
        // and don't look like incoming changes to react to.
        try {
            profileRepository.pull(keeperId)
        } catch (e: Exception) {
            hadFailure = true
        }
        try {
            storeRepository.pull(keeperId)
        } catch (e: Exception) {
            hadFailure = true
        }
        return if (hadFailure) Result.retry() else Result.success()
    }

    private suspend fun pushStore(entry: SyncQueueEntity, keeperId: String) {
        if (entry.operation == SyncOperation.DELETE) {
            val cloudId = entry.cloudId?.toLongOrNull() ?: return
            supabaseClient.from("stores").delete { filter { eq("id", cloudId) } }
            return
        }

        val store = gson.fromJson(entry.payload, Store::class.java) ?: return
        val payload = StoreCloudPayload(
            name = store.name,
            description = store.description,
            address = store.address,
            logo = store.logo,
            status = store.status,
            keeperId = keeperId
        )
        val cloudId = store.cloudId ?: entry.cloudId?.toLongOrNull()
        if (cloudId == null) {
            val inserted = supabaseClient.from("stores")
                .insert(payload) { select() }
                .decodeSingle<StoreCloudId>()
            storeDao.updateCloudId(store.id, inserted.id)
        } else {
            supabaseClient.from("stores").update(payload) { filter { eq("id", cloudId) } }
        }
    }

    private suspend fun pushProfile(entry: SyncQueueEntity) {
        val userId = entry.cloudId ?: return
        if (entry.operation == SyncOperation.DELETE) {
            supabaseClient.from("profiles").delete { filter { eq("user_id", userId) } }
            return
        }

        val profile = gson.fromJson(entry.payload, UserProfile::class.java) ?: return
        val payload = ProfileCloudPayload(
            userId = userId,
            username = profile.username,
            fullNames = profile.fullNames,
            phone = profile.phone,
            avatarUrl = profile.avatarUrl,
            about = profile.about,
            defaultAddress = profile.defaultAddress
        )
        supabaseClient.from("profiles").upsert(payload) { onConflict = "user_id" }
    }

    companion object {
        private val gson = Gson()
    }
}

object SyncScheduler {
    private const val PERIODIC_WORK_NAME = "sync_periodic"
    private const val ONE_TIME_WORK_NAME = "sync_now"

    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    /** Safety-net periodic drain, in case a triggered run never fired or failed silently. */
    fun schedulePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(30, TimeUnit.MINUTES)
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    /** Requests a near-immediate drain after a local write enqueues something to push. */
    fun triggerNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            ONE_TIME_WORK_NAME,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}
