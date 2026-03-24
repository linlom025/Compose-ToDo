package com.wisnu.kurniawan.composetodolist.foundation.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppAuthGate {
    private val lock = Any()
    private val _session = MutableStateFlow(AuthSession())
    val session: StateFlow<AuthSession> = _session.asStateFlow()

    private var observerRegistered = false
    private var hasForegrounded = false
    private var movedToBackground = false

    fun initialize() {
        synchronized(lock) {
            if (observerRegistered) return
            observerRegistered = true
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    handleProcessStart()
                }

                override fun onStop(owner: LifecycleOwner) {
                    handleProcessStop()
                }
            }
        )
    }

    fun unlock() {
        synchronized(lock) {
            val current = _session.value
            if (!current.isLocked) return
            _session.value = current.copy(isLocked = false)
        }
    }

    fun setGateEnabled(enabled: Boolean, lockImmediately: Boolean = true) {
        synchronized(lock) {
            val current = _session.value
            if (current.isGateEnabled == enabled) return
            _session.value = current.copy(
                isGateEnabled = enabled,
                isLocked = if (enabled) lockImmediately else false,
                lockVersion = if (enabled && lockImmediately) current.lockVersion + 1 else current.lockVersion
            )
        }
    }

    private fun handleProcessStart() {
        synchronized(lock) {
            if (!_session.value.isGateEnabled) {
                hasForegrounded = true
                movedToBackground = false
                if (_session.value.isLocked) {
                    _session.value = _session.value.copy(isLocked = false)
                }
                return
            }
            val shouldLock = !hasForegrounded || movedToBackground
            hasForegrounded = true
            movedToBackground = false
            if (shouldLock) {
                lockNow()
            }
        }
    }

    private fun handleProcessStop() {
        synchronized(lock) {
            if (hasForegrounded) {
                movedToBackground = true
            }
        }
    }

    private fun lockNow() {
        val current = _session.value
        _session.value = AuthSession(
            isGateEnabled = current.isGateEnabled,
            isLocked = true,
            lockVersion = current.lockVersion + 1
        )
    }
}

data class AuthSession(
    val isGateEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val lockVersion: Long = 0L
)
