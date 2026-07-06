package com.example.friendzone.testutil

import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.whenever
import org.mockito.stubbing.OngoingStubbing

fun <T> wheneverSuspend(stub: suspend () -> T): OngoingStubbing<T> =
    runBlocking { whenever(stub()) }
