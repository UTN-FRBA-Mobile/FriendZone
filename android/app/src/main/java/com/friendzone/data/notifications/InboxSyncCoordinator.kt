package com.example.friendzone.data.notifications

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InboxSyncCoordinator @Inject constructor() {
    private val _invalidations = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val invalidations: SharedFlow<Unit> = _invalidations.asSharedFlow()

    fun invalidateInbox() {
        _invalidations.tryEmit(Unit)
    }
}
