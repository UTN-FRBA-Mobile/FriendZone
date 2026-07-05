# Guía Técnica para Backend - Integración de Funcionalidades de Eliminar/Salir de Eventos

## 📋 Resumen de cambios en Frontend

El frontend está listo para:
1. Mostrar botones de "Delete Event" (para organizador) y "Leave Event" (para participante)
2. Llamar a los endpoints: `DELETE /events/{id}` y `POST /events/{id}/leave`
3. Escuchar eventos WebSocket `EVENT_DELETED` y `PARTICIPANT_LEFT`
4. Navegar automáticamente cuando se elimina o sale de un evento

---

## 🔧 Implementación requerida en Backend

### 1. **Endpoint: DELETE /events/{id}** (actualizar)

**Cambios necesarios:**

```
DELETE /events/{id}

Acciones a realizar:
1. Validar que el usuario es el organizador del evento
2. Cambiar estado del evento a CANCELLED
3. Obtener lista de todos los participantes del evento
4. Para cada participante:
   a. Crear notificación de tipo EVENT_DELETED
   b. Guardar en bandeja de entrada (InboxNotification)
5. Emitir evento WebSocket EVENT_DELETED a todos los participantes conectados
6. Retornar success response

Respuesta:
{
    "success": true,
    "message": "Event deleted successfully"
}

Errores posibles:
- 401: No autenticado
- 403: No es organizador del evento
- 404: Evento no encontrado
- 500: Error interno
```

**Ejemplo de Notificación a crear:**
```kotlin
// Pseudo-código
fun notifyEventDeleted(eventId: String, organizerId: String) {
    val event = Event.find(eventId)
    val organizer = User.find(organizerId)
    
    event.participants.forEach { participant ->
        InboxNotification.create(
            type = AppNotificationType.EVENT_DELETED,
            userId = participant.userId,
            title = "Event Cancelled",
            body = "${organizer.displayName} cancelled the event '${event.title}'",
            data = mapOf(
                "eventId" to event.id,
                "eventName" to event.title,
                "organizerId" to organizer.id,
                "organizerName" to organizer.displayName
            )
        )
    }
}
```

**Ejemplo de WebSocket Event:**
```kotlin
// Emitir a todos los participantes conectados
socketIO.to("event_${eventId}").emit("event:deleted", {
    eventId: eventId,
    reason: "Organizer cancelled the event",
    cancelledBy: organizerId
})
```

---

### 2. **Endpoint: POST /events/{id}/leave** (crear nuevo)

**Especificación:**

```
POST /events/{id}/leave

Cuerpo: (vacío o parámetros opcionales)
{
    // opcional: motivo por el que se va
    "reason": "string" (opcional)
}

Autenticación: Requerida (usuario debe estar logeado)

Acciones a realizar:
1. Obtener el evento por ID
2. Validar que el evento existe
3. Obtener el usuario actual
4. Buscar participante (evento + usuario)
5. Validar que el usuario NO es el organizador
   - Si es organizador, retornar error 403
6. Remover el participante del evento
7. Emitir evento WebSocket PARTICIPANT_LEFT
8. Opcionalmente: notificar al organizador
9. Retornar EventParticipant actualizado

Respuesta:
{
    "success": true,
    "message": "Left event successfully"
}

Errores posibles:
- 401: No autenticado
- 403: No puede salir (es el organizador o no es participante)
- 404: Evento no encontrado
- 500: Error interno
```

**Ejemplo de WebSocket Event:**
```kotlin
// Emitir a todos los participantes del evento
socketIO.to("event_${eventId}").emit("participant:left", {
    eventId: eventId,
    userId: currentUserId,
    displayName: currentUser.displayName,
    timestamp: now()
})
```

---

## 🔌 WebSocket Events (Frontend está escuchando)

### Event 1: EVENT_DELETED
```kotlin
// Frontend escucha esto
SocketEventType.EVENT_DELETED

// Payload esperado:
{
    eventId: "...",
    reason?: "..."
}

// Acción del frontend:
- Mostrar toast "Event deleted"
- Navegar atrás automáticamente
```

### Event 2: PARTICIPANT_LEFT
```kotlin
// Frontend escucha esto
SocketEventType.PARTICIPANT_LEFT

// Payload esperado:
{
    eventId: "...",
    userId: "...",
    displayName: "..."
}

// Acción del frontend:
- Refresca la lista de participantes
- Actualiza el mapa si estaba visible
```

---

## 📊 Cambios en tipos y enums (Frontend)

```kotlin
// SocketEventType - Agregados:
enum class SocketEventType {
    // ... existentes ...
    EVENT_DELETED,      // Nuevo
    PARTICIPANT_LEFT,   // Nuevo
    // ... existentes ...
}

// AppNotificationType - Agregado:
enum class AppNotificationType {
    // ... existentes ...
    EVENT_DELETED,      // Nuevo
    // ... existentes ...
}

// Nuevos Payloads:
@Serializable
data class EventDeletedPayload(
    val reason: String? = null,
)

@Serializable
data class ParticipantLeftPayload(
    val userId: String,
    val displayName: String,
)
```

---

## 🧪 Testing sugerido

### Caso 1: Organizador elimina evento
```
1. Usuario A crea un evento
2. Usuario B se une al evento
3. Usuario A presiona "Delete Event"
4. ✓ El evento desaparece para ambos usuarios
5. ✓ Ambos reciben notificación
6. ✓ Ambos salen de la pantalla automáticamente
```

### Caso 2: Participante sale del evento
```
1. Usuario A crea un evento
2. Usuario B se une al evento
3. Usuario B presiona "Leave Event"
4. ✓ Usuario B recibe confirmación
5. ✓ Usuario A ve que B abandonó el evento
6. ✓ La lista de participantes se actualiza en tiempo real
```

### Caso 3: Usuario sin conexión
```
1. Usuario A está offline
2. Usuario B (organizador) elimina el evento
3. Cuando A se conecta:
   ✓ Recibe la notificación en la bandeja
   ✓ El evento no aparece en su lista
```

---

## 🔐 Consideraciones de seguridad

1. **Validar autorización:**
   - DELETE /events/{id}: Solo organizador puede eliminar
   - POST /events/{id}/leave: Usuario debe ser participante (pero NO organizador)

2. **Validar estado del evento:**
   - No permitir acciones si el evento ya está en estado COMPLETED o CANCELLED

3. **Validar permisos:**
   - Verificar que el usuario existe
   - Verificar que el evento existe
   - Verificar que la relación usuario-evento existe

---

## 📝 Flujo de datos

```
┌─────────────────────────────────────────────────────────┐
│ Frontend - EventDetailScreen                             │
│                                                           │
│ [Delete Event] / [Leave Event] Button                   │
│          ↓                                                │
│ Confirmación del usuario                                │
│          ↓                                                │
│ DELETE /events/{id} o POST /events/{id}/leave           │
└─────────────┬───────────────────────────────────────────┘
              │
              ↓
┌─────────────────────────────────────────────────────────┐
│ Backend - API                                            │
│                                                           │
│ Validar autorización                                     │
│ Procesar acción (delete/leave)                           │
│ Crear notificaciones                                     │
│ Emitir WebSocket events                                 │
└─────────────┬───────────────────────────────────────────┘
              │
      ┌───────┴────────┐
      ↓                 ↓
┌──────────────┐  ┌──────────────┐
│ WebSocket    │  │ Notificaciones
│ EVENT_DELETED│  │ (Bandeja)
│ o            │  │
│ PARTICIPANT_ │  │ • InboxNotification
│ LEFT         │  │   creadas en BD
└──────────────┘  └──────────────┘
      │                 │
      └─────────────────┴─────────────────┐
                                           ↓
                        ┌──────────────────────────────┐
                        │ Frontend - EventDetailViewModel
                        │                              │
                        │ Socket listener recibe eventos
                        │          ↓                   │
                        │ Actualiza UI                 │
                        │ Navega atrás si se elimina   │
                        │ Refresca si participante se  │
                        │ va                           │
                        └──────────────────────────────┘
```

---

## 🚀 Orden de implementación

1. **Fase 1**: Implementar endpoint `POST /events/{id}/leave`
   - Más simple, solo requiere remover participante
   - Probar con frontend

2. **Fase 2**: Actualizar endpoint `DELETE /events/{id}`
   - Agregar notificaciones
   - Agregar emisión de WebSocket

3. **Fase 3**: Testing completo
   - Test con múltiples usuarios
   - Test con conexiones intermitentes
   - Test de notificaciones

---

## 📞 Soporte

Si tienes preguntas sobre:
- Estructura de datos: Ver `domain/model/Models.kt`
- API existente: Ver `data/remote/api/`
- WebSocket: Ver `data/remote/websocket/EventSocketManager.kt`
- Notificaciones: Ver `NotificationsInboxApi.kt` y similar

