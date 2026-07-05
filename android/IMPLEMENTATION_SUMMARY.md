# Resumen de Implementación - Funcionalidades de Eliminar y Salir de Eventos

## ✅ Lo que se ha implementado:

### 1. **Backend - Socket Events (WebSocket)**
Se agregaron dos nuevos tipos de eventos para notificar cambios en tiempo real:

- **EVENT_DELETED**: Notifica cuando un organizador elimina un evento
  - Payload: `EventDeletedPayload` (opcional: motivo de la eliminación)
  
- **PARTICIPANT_LEFT**: Notifica cuando un participante abandona un evento
  - Payload: `ParticipantLeftPayload` (userId, displayName)

**Archivos modificados:**
- `data/remote/websocket/SocketEvent.kt`

---

### 2. **Modelo de Datos**
Se agregó un nuevo tipo de notificación:

- **EVENT_DELETED**: Tipo de notificación para eventos eliminados

**Archivos modificados:**
- `domain/model/Models.kt`

---

### 3. **API - Endpoints**
Se agregó un nuevo endpoint REST:

- **POST `/events/{id}/leave`**: Permite a un usuario salir de un evento
  - El usuario será removido como participante
  - Se notificará al organizador y otros participantes

**Archivos modificados:**
- `data/remote/api/EventsApi.kt`

---

### 4. **Repositorio**
Se agregó un nuevo método:

- `leave(id: String): ApiResult<Unit>`: Permite que un participante salga de un evento

**Archivos modificados:**
- `domain/repository/Repositories.kt` (interfaz)
- `data/repository/EventRepositoryImpl.kt` (implementación)

---

### 5. **ViewModel - Lógica de Presentación**
Se agregaron nuevos estados y métodos en `EventDetailViewModel`:

**Nuevos estados:**
- `deleteEventState: StateFlow<EventActionState>`: Estado de la acción de eliminar evento
- `leaveEventState: StateFlow<EventActionState>`: Estado de la acción de salir del evento

**Nuevos métodos:**
- `deleteEvent()`: Elimina el evento (solo organizador)
- `leaveEvent()`: Sale del evento (solo participantes)
- `resetDeleteEventState()`: Resetea el estado de eliminación
- `resetLeaveEventState()`: Resetea el estado de salida

**Escucha de Socket Events:**
- Se agregó lógica para escuchar `EVENT_DELETED` y `PARTICIPANT_LEFT` del socket
- Cuando se recibe `EVENT_DELETED`, se cierra automáticamente la pantalla de detalles

**Archivos modificados:**
- `presentation/events/EventDetailViewModel.kt`

---

### 6. **UI - Pantalla de Detalles del Evento**
Se agregaron nuevos elementos en `EventDetailScreen`:

**Nuevos botones:**
- **"Delete Event"**: Visible solo para el organizador
- **"Leave Event"**: Visible solo para participantes (no organizador)

**Nuevos diálogos de confirmación:**
- `ConfirmDeleteEventDialog`: Confirma la eliminación del evento
- `ConfirmLeaveEventDialog`: Confirma que el usuario quiere salir

**Manejo de eventos:**
- Se muestran mensajes de toast al completar/fallar las acciones
- Se navega atrás automáticamente al completar la acción exitosamente
- Se muestran errores al usuario si algo falla

**Archivos modificados:**
- `presentation/events/EventDetailScreen.kt`

---

## 🔧 Lo que necesita implementarse en el Backend:

### 1. **Endpoint DELETE para eliminar evento**
El endpoint ya existe, pero debe:
- ✅ Eliminar el evento de la BD
- 🔲 **TODO**: Enviar notificaciones a todos los participantes usando la misma API de notificaciones
- 🔲 **TODO**: Emitir evento `EVENT_DELETED` por WebSocket a todos los participantes conectados

### 2. **Endpoint POST para salir del evento**
Debe implementarse:
- 🔲 Remover al usuario como participante del evento
- 🔲 Emitir evento `PARTICIPANT_LEFT` por WebSocket a todos los demás participantes
- 🔲 Opcionalmente: notificar al organizador

### 3. **Sistema de Notificaciones**
Para reutilizar la misma API de notificaciones como solicitaste:
- 🔲 Crear un tipo de notificación `EVENT_DELETED`
- 🔲 Usar la misma API que se usa para otras notificaciones
- 🔲 Guardar en la bandeja de entrada (InboxNotification)

**Ejemplo de estructura:**
```kotlin
// Evento de notificación
{
    type: "EVENT_DELETED",
    title: "Event Cancelled",
    body: "{organizerName} cancelled the event {eventName}",
    eventId: "...",
    eventName: "...",
    organizerId: "..."
}
```

---

## 📱 Flujo de uso:

### Como Organizador:
1. El organizador está viendo los detalles del evento
2. Presiona el botón "Delete Event"
3. Se muestra un diálogo de confirmación
4. Al confirmar, se envía DELETE /events/{id}
5. El backend:
   - Elimina el evento
   - Envía notificaciones a todos los participantes
   - Emite evento EVENT_DELETED por WebSocket
6. Todos los participantes conectados reciben `EVENT_DELETED` por socket y salen de la pantalla automáticamente
7. Los participantes no conectados recibirán la notificación en su bandeja de entrada

### Como Participante:
1. El participante está viendo los detalles del evento
2. Presiona el botón "Leave Event"
3. Se muestra un diálogo de confirmación
4. Al confirmar, se envía POST /events/{id}/leave
5. El backend:
   - Remueve al participante del evento
   - Emite evento PARTICIPANT_LEFT por WebSocket
6. Otros participantes reciben la actualización en tiempo real
7. El usuario que salió es llevado de vuelta a la pantalla principal

---

## 🎯 Próximos pasos:

1. **Backend**:
   - Implementar la lógica en el endpoint DELETE para eliminar evento
   - Implementar el endpoint POST /events/{id}/leave
   - Integrar con el sistema de notificaciones existente
   - Emitir los eventos por WebSocket

2. **Frontend** (opcional):
   - Agregar animaciones de transición más suaves
   - Agregar loading skeleton mientras se procesa
   - Agregar opción de "undo" con timeout (si el backend lo soporta)

---

## 📝 Notas:
- Todos los cambios en el frontend son automáticos al recibir eventos por WebSocket
- Las notificaciones se enviarán automáticamente desde el backend
- El sistema reutiliza la infraestructura existente de WebSocket y notificaciones

