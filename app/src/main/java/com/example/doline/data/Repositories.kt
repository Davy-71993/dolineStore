package com.example.doline.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContracts
import com.example.doline.sha256
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.coroutines.withContext
import javax.inject.Inject

private val syncPayloadGson = Gson()

// Enqueues a push for a local Store write and wakes the worker to drain it soon. All stores
// belong to the device owner, so every write is queued unconditionally.
private suspend fun SyncQueueDao.enqueueStoreSync(context: Context, store: Store, operation: SyncOperation) {
    enqueue(
        SyncQueueEntity(
            entityType = SyncEntityType.STORE,
            localId = store.id,
            operation = operation,
            payload = syncPayloadGson.toJson(store),
            cloudId = store.cloudId?.toString()
        )
    )
    SyncScheduler.triggerNow(context)
}

// Enqueues a push for a local UserProfile write and wakes the worker to drain it soon. Only the
// device owner's profile (the one synced from Supabase auth) has a userId; local-only profiles
// (staff/client/supplier) have no cloud counterpart to push to, so those writes are skipped.
private suspend fun SyncQueueDao.enqueueProfileSync(context: Context, profile: UserProfile, operation: SyncOperation) {
    val userId = profile.userId ?: return
    enqueue(
        SyncQueueEntity(
            entityType = SyncEntityType.PROFILE,
            localId = profile.id,
            operation = operation,
            payload = syncPayloadGson.toJson(profile),
            cloudId = userId
        )
    )
    SyncScheduler.triggerNow(context)
}

class AuthRepository @Inject constructor(
    private val auth: Auth
){
    val sessionStatus: Flow<SessionStatus> = auth.sessionStatus
    val currentUser: UserInfo? get() = auth.currentUserOrNull()
    val currentSession: UserSession? get() = auth.currentSessionOrNull()
    // Returns the new auth user's id on success, even if email confirmation is still pending -
    // signUpWith returns the created user immediately, a session only shows up once confirmed.
    suspend fun register(email: String, password: String): String? {
        return try {
            auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }?.id
        } catch (e: Exception) {
            null
        }
    }
    suspend fun login(email: String, password: String): Boolean {
        return try {
            auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    // Opens the system browser for Google's OAuth consent screen and returns as soon as it's
    // launched - it does not wait for sign-in to complete. The redirect back (doline://auth)
    // is handled by MainActivity.handleDeeplinks, which exchanges the code and updates
    // [sessionStatus]; callers should observe that flow to react to the sign-in completing.
    suspend fun signInWithGoogle(): Boolean {
        return try {
            auth.signInWith(Google)
            true
        } catch (e: Exception) {
            false
        }
    }
    suspend fun resendVerificationEmail(email: String) {
        auth.resendEmail(
            type = OtpType.Email.SIGNUP,
            email = email
        )
    }
    suspend fun logout() {
        try {
            auth.signOut()
        } catch (e: Exception) {
            // Log error if needed, but signOut usually succeeds even on network issues
            e.printStackTrace()
        }
    }
    suspend fun refreshSession() {
        try {
            auth.refreshCurrentSession()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun isUserAuthenticated(): Boolean {
        return currentSession != null && currentUser != null
    }

}

class UserProfileRepository @Inject constructor(
    private  val dao: UserProfileDao,
    private  val supabaseClient: SupabaseClient,
    private val syncQueueDao: SyncQueueDao,
    private val syncStateDao: SyncStateDao,
    @ApplicationContext private val context: Context
){
    fun getProfileById(id: Long): Flow<UserProfile?> = dao.getProfileById(id)
    fun getProfileByUserId(userId: String): Flow<UserProfile?> = dao.getProfileByUserId(userId)
    suspend fun getProfileByUserIdOnce(userId: String): UserProfile? = dao.getProfileByUserIdOnce(userId)

    suspend fun insertProfile(profile: UserProfile): Long {
        val id = dao.insertProfile(profile)
        syncQueueDao.enqueueProfileSync(context, profile.copy(id = id), SyncOperation.INSERT)
        return id
    }

    suspend fun updateProfile(profile: UserProfile) {
        dao.updateProfile(profile)
        syncQueueDao.enqueueProfileSync(context, profile, SyncOperation.UPDATE)
    }

    suspend fun deleteProfile(id: Long) {
        val existing = dao.getProfileByIdOnce(id)
        dao.deleteProfileById(id)
        existing?.let { syncQueueDao.enqueueProfileSync(context, it, SyncOperation.DELETE) }
    }

    // Updates the owner's existing local profile row if one already exists for this cloud
    // userId, instead of piling up a duplicate on every login. Returns the local row id.
    // Writes via the DAO directly, not the enqueue-wrapped methods above: this is pulling data
    // FROM the cloud, so it must not turn around and queue a push back for it.
    suspend fun upsertCloudProfile(profile: UserProfile): Long {
        val userId = profile.userId ?: return dao.insertProfile(profile)
        val existing = dao.getProfileByUserIdOnce(userId)
        return if (existing != null) {
            dao.updateProfile(profile.copy(id = existing.id))
            existing.id
        } else {
            dao.insertProfile(profile)
        }
    }

    suspend fun fetchProfileFromCloud(userId: String): UserProfile {
        return withContext(Dispatchers.IO){
            supabaseClient.from("profiles").select(
                Columns.list("userId:user_id, phone, username:name, fullNames:full_name, avatarUrl:avatar_url, about, defaultAddress:default_address")
            ) {
                filter {
                    eq("user_id", userId)
                }
            }
        }.decodeSingle<UserProfile>()
    }

    // Pulls the owner's profile from Supabase and upserts it locally, then records when this
    // ran in sync_state. lastPulledAt isn't used to filter the cloud query yet - profiles is a
    // single row per owner, so a full fetch every pull is cheap - but it establishes the
    // watermark for when other entities need real incremental filtering.
    //
    // A missing cloud row isn't a failure - it just means nothing has created a profile for
    // this user yet (right after email/password registration, before the local profile is
    // pushed up, or a first-time OAuth sign-in that never went through the register form at
    // all) - so it's swallowed here and handed to ensureLocalProfile instead of surfaced as an
    // error to whoever is hydrating the app after sign-in.
    suspend fun pull(userId: String) {
        val cloudProfile = try {
            fetchProfileFromCloud(userId)
        } catch (e: Exception) {
            ensureLocalProfile(userId)
            return
        }
        upsertCloudProfile(cloudProfile)
        syncStateDao.upsert(SyncStateEntity(SyncEntityType.PROFILE, System.currentTimeMillis()))
    }

    // Covers a first-time OAuth sign-in (e.g. Google): no local profile exists yet because the
    // user never went through the register form, and no cloud one exists because nothing has
    // ever created one. Seeds a minimal profile from whatever Supabase auth already knows about
    // the user - the OAuth provider's metadata for a Google identity includes their name/avatar
    // - and lets it push up through the normal outbox. A no-op if a local profile already
    // exists for this user, or if the current session isn't actually this user.
    suspend fun ensureLocalProfile(userId: String) {
        if (dao.getProfileByUserIdOnce(userId) != null) return
        val user = supabaseClient.auth.currentUserOrNull()?.takeIf { it.id == userId } ?: return
        val metadata = user.userMetadata
        val fullNames = metadata?.get("full_name")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("name")?.jsonPrimitive?.contentOrNull
        val avatarUrl = metadata?.get("avatar_url")?.jsonPrimitive?.contentOrNull
            ?: metadata?.get("picture")?.jsonPrimitive?.contentOrNull
        insertProfile(
            UserProfile(
                userId = userId,
                email = user.email,
                fullNames = fullNames,
                avatarUrl = avatarUrl
            )
        )
    }
}

class StoreRepository @Inject constructor(
    private val dao: StoreDao,
    private val supabaseClient: SupabaseClient,
    private val syncQueueDao: SyncQueueDao,
    private val syncStateDao: SyncStateDao,
    @ApplicationContext private val context: Context
) {
    fun getStoreById(storeId: Long): Flow<Store?> = dao.getStoreById(storeId)
    fun getStoreWithItems(storeId: Long): Flow<StoreWithItems?> = dao.getStoreWithItems(storeId)
    fun getAllStores(): Flow<List<Store>> = dao.getAllStores()

    suspend fun insertStore(store: Store): Long {
        val id = dao.insertStore(store)
        syncQueueDao.enqueueStoreSync(context, store.copy(id = id), SyncOperation.INSERT)
        return id
    }

    suspend fun updateStore(store: Store) {
        dao.update(store)
        syncQueueDao.enqueueStoreSync(context, store, SyncOperation.UPDATE)
    }

    suspend fun deleteStore(store: Store) {
        dao.delete(store)
        syncQueueDao.enqueueStoreSync(context, store, SyncOperation.DELETE)
    }
    suspend fun fetchStoresFromCloud(userId: String): List<Store> {
        return withContext(Dispatchers.IO) {
            val result = supabaseClient.from("stores")
                .select(Columns.list("cloudId:id, name, description, address, logo, status, items:ads(name:title, description, specs, categorySlug:category_id, subCategorySlug:sub_category_id, quantity, units)")){
                    filter {
                        eq("keeper_id", userId)
                    }
                }.decodeList<Store>()
            result
        }
    }

    // Matches a cloud-fetched store to its local row by cloudId, updating it in place if one
    // already exists instead of inserting a duplicate on every pull. Writes via the DAO
    // directly, not insertStore/updateStore: this is pulling data FROM the cloud, so it must
    // not turn around and queue a push back for it.
    suspend fun upsertCloudStore(store: Store): Long {
        val existing = store.cloudId?.let { dao.getStoreByCloudId(it) }
        return if (existing != null) {
            dao.update(store.copy(id = existing.id))
            existing.id
        } else {
            dao.insertStore(store)
        }
    }

    // Pulls all of the owner's stores from Supabase and upserts them locally, then records when
    // this ran in sync_state. lastPulledAt isn't used to filter the cloud query yet - a store
    // owner only has a handful of stores, so a full fetch every pull is cheap - but it
    // establishes the watermark for when incremental filtering is worth adding.
    suspend fun pull(keeperId: String) {
        val stores = fetchStoresFromCloud(keeperId)
        stores.forEach { upsertCloudStore(it) }
        syncStateDao.upsert(SyncStateEntity(SyncEntityType.STORE, System.currentTimeMillis()))
    }
}

class ItemRepository @Inject constructor(
    private val dao: ItemDao,
    private val upcApiService: UPCApiService
) {
    fun getAllItems(storeId: Long): Flow<List<ItemWithBatches>> = dao.getAllItems(storeId)
    fun getItemById(itemId: Long): Flow<ItemWithBatches?> = dao.getItemById(itemId)
    fun getItem(itemId: Long): Flow<ItemEntity?> = dao.getItem(itemId)
    fun getAllActiveItems(storeID: Long): Flow<List<ItemWithBatches>> {
        return dao.getAllItems(storeID)
            .map { list ->
                list.map { iwb -> iwb.copy(batches = iwb.batches.filter { it.batch.quantity > 0 }) }
            }
    }
    fun getAllOutOfStockItems(storeID: Long): Flow<List<ItemWithBatches>> {
        return dao.getAllItems(storeID)
            .map { list ->
                list.map { iwb ->
                    iwb.copy(batches = iwb.batches.filter { it.batch.quantity == 0.toDouble() })
                }
            }
    }
    suspend fun fetchInitialItemDetails(upc: String): UPCItem? {
        return try {
            val response = upcApiService.lookupUPC(upc)
            response.items?.firstOrNull()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    suspend fun insertItem(item: ItemEntity) = dao.insert(item)
    suspend fun bulkInsertItems(items: List<ItemEntity>) = dao.bulkInsert(items)
    suspend fun updateItem(item: ItemEntity) = dao.update(item)
    suspend fun deleteItem(item: ItemEntity) = dao.delete(item)
    suspend fun decreaseQuantity(batchId: Long, amount: Double) = dao.decreaseQuantity(batchId, amount)
    suspend fun increaseQuantity(batchId: Long, amount: Double) = dao.increaseQuantity(batchId, amount)
}

class PricingRepository @Inject constructor(
    private val dao: PricingDao
) {
    fun getItemPricings(itemId: Long) = dao.getAllPricings(itemId)
    fun getPricingById(id: Long) = dao.getPricingById(id)
    suspend fun getPrevPrices(batchId: Long) = dao.getPrevPrices(batchId)
    suspend fun insertPricing(pricing: Pricing) = dao.insert(pricing)
    suspend fun updatePricing(pricing: Pricing) = dao.update(pricing)
    suspend fun deletePricing(pricing: Pricing) = dao.delete(pricing)
}

class BatchRepository @Inject constructor(
    private val dao: BatchDao,
){
    suspend fun insertBatch(batch: BatchEntity) = dao.insertBatch(batch)
    suspend fun insertBatchPricings(pairs: List<BatchPricingCrossRef>) = dao.insertBatchPricings(pairs)
    suspend fun decreaseQuantity(batchId: Long, qty: Double) = dao.decreaseQuantity(batchId, qty)
    suspend fun increaseQuantity(batchId: Long, qty: Double) = dao.increaseQuantity(batchId, qty)
    suspend fun getBatchWithItem(batchId: Long) = dao.getBatchWithItem(batchId)
    suspend fun deletePricing(pair: BatchPricingCrossRef) = dao.deletePricing(pair)
}

class CategoryRepository @Inject constructor(
    private val dao: CategoryDao,
    private  val supabaseClient: SupabaseClient
) {
    fun getAllCategories(): Flow<List<CategoryEntity>> = dao.getAllCategories()
    fun getAllCategoriesWithSubCategories(): Flow<List<CategoryWithSubCategories>> = dao.getAllCategoriesWithSubCategories()
    fun getCategoryBySlug(slug: String): Flow<CategoryEntity?> = dao.getCategoryBySlug(slug)
    fun getCategoryBySlugWithSubCategories(slug: String): Flow<CategoryWithSubCategories?> = dao.getCategoryBySlugWithSubCategories(slug)
    suspend fun insert(category: CategoryEntity) = dao.insert(category)
    suspend fun fetchCategoriesFromCloud(): List<SupabaseCategory> {
        return withContext(Dispatchers.IO) {
            val result = supabaseClient.from("categories")
                .select(Columns.list("name, slug, specs:default_specs, subCategories:sub_categories(name, slug, categorySlug:category_id, specs:default_specs)")).decodeList<SupabaseCategory>()
            result
        }
    }

}

class SubCategoryRepository @Inject constructor(
    private val dao: SubCategoryDao,
) {
    suspend fun insert(subCategory: SubCategory) = dao.insert(subCategory)
}

class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
) {
    fun getExpenseById(id: Long): Flow<Expense?> = dao.getExpenseById(id)
    fun getAllStoreExpenses(storeId: Long): Flow<List<Expense>> = dao.getAllStoreExpenses(storeId)
    fun getAllStoreExpensesPerDay(storeId: Long, start: Long, end: Long): Flow<List<Expense>> = dao.getAllStoreExpensesPerDay(storeId, start, end)
    suspend fun insert(expense: Expense) = dao.insert(expense)
}

class NoteRepository @Inject constructor(
    private val dao: NoteDao
){
    fun getNotes(storeId: Long): Flow<List<NotesEntity>> = dao.getAllStoreNotes(storeId)
    suspend fun insert(note: NotesEntity): Long = dao.insert(note)
}

class CartItemRepository @Inject constructor(
    private val dao: CartItemDao
){
    fun getAllItems(): Flow<List<CartItem>> = dao.getAllItems()
    suspend fun insert(item: CartItemEntity): Long = dao.insert(item)
    suspend fun editItem(item: CartItemEntity) = dao.editItem(item)
    suspend fun deleteItem(item: CartItemEntity) = dao.deleteItem(item)
    suspend fun clearCart() = dao.clearCart()
}

class OrderRepository @Inject constructor(
    private val dao: OrderDao
){
    suspend fun insert(item: OrderEntity): Long = dao.insert(item)
    fun getAllOrders(storeId: Long): Flow<List<Order>> = dao.getStoreOrders(storeId)
    fun getClientOrders(clientId: Long): Flow<List<Order>> = dao.getClientOrders(clientId)
    fun getOrderById(orderId: Long): Flow<Order> = dao.getOrderById(orderId)
    suspend fun updateCreditBalance(amount: Double, orderId: Long) = dao.updateCreditBalance(amount, orderId)
    suspend fun editOrder(item: OrderEntity) = dao.editOrder(item)
    suspend fun delete(orderId: Long) = dao.delete(orderId)
    suspend fun returnItems(items: List<OrderItemEntity>) = dao.returnItems(items)

}

class OrderItemRepository @Inject constructor(
    private val dao: OrderItemDao
){
    suspend fun insert(item: OrderItemEntity): Long = dao.insert(item)
}

class ClientRepository @Inject constructor(
    private val dao: ClientDao,
    private val profileDao: UserProfileDao
){
    suspend fun insert(client: ClientEntity): Long = dao.insert(client)
    fun getClients(storeId: Long): Flow<List<ClientWithProfile>> = dao.getClients(storeId)
    fun getClientById(clientId: Long): Flow<ClientWithProfile?> = dao.getClientById(clientId)
    suspend fun editClient(client: ClientEntity) = dao.update(client)
    suspend fun deleteClient(client: ClientEntity) {
        dao.delete(client)
        profileDao.deleteProfileById(client.profileId)
    }
}

class SupplierRepository @Inject constructor(
    private val dao: SupplierDao,
    private val profileDao: UserProfileDao
){
    suspend fun insert(supplier: SupplierEntity): Long = dao.insert(supplier)
    fun getSuppliers(storeId: Long): Flow<List<SupplierWithProfile>> = dao.getSuppliers(storeId)
    fun getSupplierById(supplierId: Long): Flow<SupplierWithProfile?> = dao.getSupplierById(supplierId)
    fun getSupplierWithItems(supplierId: Long): Flow<SupplierWithItems?> = dao.getSupplierWithItems(supplierId)
    fun getItemWithSuppliers(itemId: Long): Flow<ItemWithSuppliers?> = dao.getItemWithSuppliers(itemId)
    suspend fun editSupplier(supplier: SupplierEntity) = dao.update(supplier)
    suspend fun deleteSupplier(supplier: SupplierEntity) {
        dao.delete(supplier)
        profileDao.deleteProfileById(supplier.profileId)
    }
    suspend fun linkItemToSupplier(itemId: Long, supplierId: Long) =
        dao.linkItemToSupplier(ItemSupplierCrossRef(itemId, supplierId))
    suspend fun unlinkItemFromSupplier(itemId: Long, supplierId: Long) =
        dao.unlinkItemFromSupplier(ItemSupplierCrossRef(itemId, supplierId))
}

class StaffRepository @Inject constructor(
    private val dao: StaffDao,
    private val profileDao: UserProfileDao
){
    suspend fun insert(staff: StaffEntity): Long = dao.insert(staff)
    fun getStaffs(storeId: Long): Flow<List<StaffWithProfile>> = dao.getStaffs(storeId)
    fun getStaffById(staffId: Long): Flow<StaffWithProfile?> = dao.getStaffById(staffId)
    suspend fun getStaffByIdOnce(staffId: Long): StaffWithProfile? = dao.getStaffByIdOnce(staffId)
    suspend fun editStaff(staff: StaffEntity) = dao.update(staff)
    suspend fun deleteStaff(staff: StaffEntity) {
        dao.delete(staff)
        profileDao.deleteProfileById(staff.profileId)
    }

    /** Verifies [passKey] against the stored hash for [staffId], returning the staff on success. */
    suspend fun verifyPassKey(staffId: Long, passKey: String): StaffWithProfile? {
        val staff = dao.getStaffByIdOnce(staffId) ?: return null
        return if (staff.staff.passKeyHash == passKey.sha256()) staff else null
    }
}

class CreditPaymentRepository @Inject constructor(
    private val dao: CreditPaymentDao
){
    suspend fun insert(cp: CreditPayment): Long = dao.insert(cp)
    fun getCreditPayments(orderId: Long): Flow<List<CreditPayment>> = dao.getCreditPayments(orderId)
}
