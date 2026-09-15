package com.example.doline.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

data class ActiveStaffSession(
    val storeId: Long,
    val staff: StaffWithProfile
)

const val STAFF_PASS_KEY_LENGTH = 6

// Keeps a pass key field numeric-only and capped at STAFF_PASS_KEY_LENGTH digits as it's typed.
fun String.toPassKeyInput(): String = filter { it.isDigit() }.take(STAFF_PASS_KEY_LENGTH)

// Tracks which staff member is clocked in on this device and auto-clocks them out when idle.
@Singleton
class StaffSessionManager @Inject constructor() {

    companion object {
        const val IDLE_TIMEOUT_MS = 60_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var idleJob: Job? = null

    private val _session = MutableStateFlow<ActiveStaffSession?>(null)
    val session: StateFlow<ActiveStaffSession?> = _session.asStateFlow()

    fun login(storeId: Long, staff: StaffWithProfile) {
        _session.value = ActiveStaffSession(storeId, staff)
        restartIdleTimer()
    }

    fun logout() {
        idleJob?.cancel()
        idleJob = null
        _session.value = null
    }

    /** Call on any user interaction while a staff session is active to reset the idle clock. */
    fun notifyActivity() {
        if (_session.value != null) restartIdleTimer()
    }

    private fun restartIdleTimer() {
        idleJob?.cancel()
        idleJob = scope.launch {
            delay(IDLE_TIMEOUT_MS.milliseconds)
            _session.value = null
        }
    }
}
