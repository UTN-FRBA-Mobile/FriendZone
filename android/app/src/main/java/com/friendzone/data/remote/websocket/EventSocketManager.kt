package com.example.friendzone.data.remote.websocket

import com.example.friendzone.BuildConfig
import com.example.friendzone.data.local.TokenManager
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.callbackFlow
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventSocketManager @Inject constructor(
    private val tokenManager: TokenManager,
) {
    private var socket: Socket? = null
    private val joinedEventIds = mutableSetOf<String>()
    private var hasConnectedBefore = false
    private val _events = MutableSharedFlow<SocketEvent>(extraBufferCapacity = 32)
    val events = _events.asSharedFlow()

    suspend fun connect() {
        if (socket?.connected() == true) return

        val token = tokenManager.getAccessToken() ?: return
        val options = IO.Options.builder()
            .setAuth(mapOf("token" to token))
            .setForceNew(true)
            .build()

        val baseUrl = BuildConfig.API_BASE_URL.trimEnd('/')
        socket = IO.socket(baseUrl, options).also { s ->
            registerListeners(s)
            s.connect()
        }
    }

    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        joinedEventIds.clear()
        hasConnectedBefore = false
    }

    fun joinEvent(eventId: String) {
        synchronized(joinedEventIds) {
            joinedEventIds.add(eventId)
        }
        emitJoin(eventId)
    }

    fun leaveEvent(eventId: String) {
        synchronized(joinedEventIds) {
            joinedEventIds.remove(eventId)
        }
        socket?.emit("leave", JSONObject(mapOf("eventId" to eventId)))
    }

    fun connectionState(): Flow<Boolean> = callbackFlow {
        val s = socket
        if (s == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        val connectListener = io.socket.emitter.Emitter.Listener {
            trySend(true)
        }
        val disconnectListener = io.socket.emitter.Emitter.Listener {
            trySend(false)
        }
        s.on(Socket.EVENT_CONNECT, connectListener)
        s.on(Socket.EVENT_DISCONNECT, disconnectListener)
        trySend(s.connected())
        awaitClose {
            s.off(Socket.EVENT_CONNECT, connectListener)
            s.off(Socket.EVENT_DISCONNECT, disconnectListener)
        }
    }

    private fun registerListeners(socket: Socket) {
        socket.on(Socket.EVENT_CONNECT) {
            onSocketConnected()
        }
        listen(socket, "location.updated", SocketEventType.LOCATION_UPDATED)
        listen(socket, "participant.joined", SocketEventType.PARTICIPANT_JOINED)
        listen(socket, "participant.arrived", SocketEventType.PARTICIPANT_ARRIVED)
        listen(socket, "event.completed", SocketEventType.EVENT_COMPLETED)
    }

    private fun onSocketConnected() {
        rejoinTrackedEvents()
        if (hasConnectedBefore) {
            _events.tryEmit(SocketEvent(SocketEventType.RECONNECTED, "", ""))
        }
        hasConnectedBefore = true
    }

    private fun rejoinTrackedEvents() {
        val eventIds = synchronized(joinedEventIds) {
            joinedEventIds.toList()
        }
        eventIds.forEach { emitJoin(it) }
    }

    private fun emitJoin(eventId: String) {
        socket?.emit("join", JSONObject(mapOf("eventId" to eventId)))
    }

    private fun listen(socket: Socket, eventName: String, type: SocketEventType) {
        socket.on(eventName) { args ->
            val payload = args.firstOrNull()?.toString().orEmpty()
            val eventId = extractEventId(payload)
            _events.tryEmit(SocketEvent(type, eventId, payload))
        }
    }

    private fun extractEventId(payload: String): String {
        return runCatching {
            JSONObject(payload).optString("eventId", "")
        }.getOrDefault("")
    }
}
