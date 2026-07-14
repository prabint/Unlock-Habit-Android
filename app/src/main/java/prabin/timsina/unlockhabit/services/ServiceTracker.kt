package prabin.timsina.unlockhabit.services

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServiceTracker @Inject constructor() {
    private val _serviceState = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _serviceState.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    fun setServiceRunning(running: Boolean) {
        _serviceState.value = running
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }
}