# Detalle de Cambios en Código - Frontend Android

## 📁 Archivos Modificados

### 1. `data/remote/websocket/SocketEvent.kt`

**Cambios:**
- Agregados dos nuevos tipos de evento al enum `SocketEventType`
- Agregadas dos nuevas payload classes para serialización

```kotlin
// AGREGADO al enum SocketEventType:
EVENT_DELETED,      // Notificación de evento eliminado
PARTICIPANT_LEFT,   // Notificación de participante que se fue

// AGREGADAS nuevas clases:
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

**Líneas afectadas:** 5-11, 40-49

---

### 2. `domain/model/Models.kt`

**Cambios:**
- Agregado nuevo tipo de notificación al enum `AppNotificationType`

```kotlin
// AGREGADO al enum AppNotificationType:
EVENT_DELETED,      // Notificación cuando un evento es eliminado
```

**Líneas afectadas:** 107-112

---

### 3. `data/remote/api/EventsApi.kt`

**Cambios:**
- Agregado nuevo endpoint Retrofit para dejar un evento

```kotlin
// AGREGADO a la interfaz EventsApi:
@POST("events/{id}/leave")
suspend fun leave(@Path("id") id: String): SuccessResponseDto
```

**Líneas afectadas:** 30

---

### 4. `domain/repository/Repositories.kt`

**Cambios:**
- Agregado nuevo método abstracto a la interfaz `EventRepository`

```kotlin
// AGREGADO a interface EventRepository:
suspend fun leave(id: String): ApiResult<Unit>
```

**Líneas afectadas:** 66

---

### 5. `data/repository/EventRepositoryImpl.kt`

**Cambios:**
- Implementado el método `leave` en la clase `EventRepositoryImpl`

```kotlin
// AGREGADO a EventRepositoryImpl:
override suspend fun leave(id: String): ApiResult<Unit> = safeApiCall {
    eventsApi.leave(id)
    Unit
}
```

**Líneas afectadas:** 82-85

---

### 6. `presentation/events/EventDetailViewModel.kt`

**Cambios principales:**

#### a) Nuevos tipos de estado
```kotlin
// AGREGADO nuevo sealed class:
sealed class EventActionState {
    data object Idle : EventActionState()
    data object Loading : EventActionState()
    data class Success(val message: String) : EventActionState()
    data class Error(val message: String) : EventActionState()
}
```

#### b) Nuevos campos en la Data class
```kotlin
// AGREGADO a EventDetailUiState.Data:
val isOrganizer: Boolean,  // Para mostrar/ocultar botones
```

#### c) Nuevos StateFlows
```kotlin
// AGREGADOS al ViewModel:
private val _deleteEventState = MutableStateFlow<EventActionState>(EventActionState.Idle)
val deleteEventState: StateFlow<EventActionState> = _deleteEventState.asStateFlow()

private val _leaveEventState = MutableStateFlow<EventActionState>(EventActionState.Idle)
val leaveEventState: StateFlow<EventActionState> = _leaveEventState.asStateFlow()
```

#### d) Escucha de nuevos eventos de socket
```kotlin
// MODIFICADO init block para agregar:
else if (event.type == SocketEventType.EVENT_DELETED) {
    _deleteEventState.value = EventActionState.Success("Event deleted")
} else if (event.type == SocketEventType.PARTICIPANT_LEFT) {
    loadDetail()
}
```

#### e) Nuevos métodos públicos
```kotlin
// AGREGADOS al ViewModel:
fun deleteEvent() {
    viewModelScope.launch {
        _deleteEventState.value = EventActionState.Loading
        when (val result = eventRepository.delete(eventId)) {
            is ApiResult.Success -> {
                _deleteEventState.value = EventActionState.Success("Event deleted successfully")
            }
            is ApiResult.Error -> {
                _deleteEventState.value = EventActionState.Error(result.error.displayMessage())
            }
            ApiResult.Loading -> Unit
        }
    }
}

fun leaveEvent() {
    viewModelScope.launch {
        _leaveEventState.value = EventActionState.Loading
        when (val result = eventRepository.leave(eventId)) {
            is ApiResult.Success -> {
                _leaveEventState.value = EventActionState.Success("Left event successfully")
            }
            is ApiResult.Error -> {
                _leaveEventState.value = EventActionState.Error(result.error.displayMessage())
            }
            ApiResult.Loading -> Unit
        }
    }
}

fun resetDeleteEventState() {
    _deleteEventState.value = EventActionState.Idle
}

fun resetLeaveEventState() {
    _leaveEventState.value = EventActionState.Idle
}
```

#### f) Actualización de buildUiState
```kotlin
// MODIFICADO return statement en buildUiState para agregar:
isOrganizer = isOrganizer(event),
```

**Líneas afectadas:** 58-71, 81-86, 122-126, 145-157, 330-371, 464-477

---

### 7. `presentation/events/EventDetailScreen.kt`

**Cambios principales:**

#### a) Nuevas colecciones de estado
```kotlin
// AGREGADAS en composable:
val deleteEventState by viewModel.deleteEventState.collectAsStateWithLifecycle()
val leaveEventState by viewModel.leaveEventState.collectAsStateWithLifecycle()
var showDeleteConfirmation by remember { mutableStateOf(false) }
var showLeaveConfirmation by remember { mutableStateOf(false) }
```

#### b) LaunchedEffect para manejar deleteEventState
```kotlin
// AGREGADO:
LaunchedEffect(deleteEventState) {
    if (deleteEventState is EventActionState.Success) {
        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
        onBack()
    } else if (deleteEventState is EventActionState.Error) {
        Toast.makeText(
            context,
            (deleteEventState as EventActionState.Error).message,
            Toast.LENGTH_LONG
        ).show()
        viewModel.resetDeleteEventState()
    }
}
```

#### c) LaunchedEffect para manejar leaveEventState
```kotlin
// AGREGADO:
LaunchedEffect(leaveEventState) {
    if (leaveEventState is EventActionState.Success) {
        Toast.makeText(context, "Left event", Toast.LENGTH_SHORT).show()
        onBack()
    } else if (leaveEventState is EventActionState.Error) {
        Toast.makeText(
            context,
            (leaveEventState as EventActionState.Error).message,
            Toast.LENGTH_LONG
        ).show()
        viewModel.resetLeaveEventState()
    }
}
```

#### d) Diálogos de confirmación
```kotlin
// AGREGADO:
if (showDeleteConfirmation) {
    ConfirmDeleteEventDialog(
        onConfirm = {
            showDeleteConfirmation = false
            viewModel.deleteEvent()
        },
        onDismiss = { showDeleteConfirmation = false },
        isLoading = deleteEventState is EventActionState.Loading,
    )
}

if (showLeaveConfirmation) {
    ConfirmLeaveEventDialog(
        onConfirm = {
            showLeaveConfirmation = false
            viewModel.leaveEvent()
        },
        onDismiss = { showLeaveConfirmation = false },
        isLoading = leaveEventState is EventActionState.Loading,
    )
}
```

#### e) Nuevos botones en la UI
```kotlin
// AGREGADO a la sección is EventDetailUiState.Data:
if (state.isOrganizer) {
    Spacer(modifier = Modifier.height(16.dp))
    FriendZoneOutlineButton(
        text = "Delete Event",
        onClick = { showDeleteConfirmation = true },
    )
} else {
    Spacer(modifier = Modifier.height(16.dp))
    FriendZoneOutlineButton(
        text = "Leave Event",
        onClick = { showLeaveConfirmation = true },
    )
}
```

#### f) Nuevas funciones composables
```kotlin
// AGREGADAS:
@Composable
private fun ConfirmDeleteEventDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    // Muestra diálogo de confirmación para eliminar evento
}

@Composable
private fun ConfirmLeaveEventDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean = false,
) {
    // Muestra diálogo de confirmación para salir del evento
}
```

**Líneas afectadas:** 52-113, 162-213, 260-323

---

## 📊 Resumen de cambios por tipo

### Adicionales (nuevas líneas)
- ✅ Nuevos tipos de Socket: 2
- ✅ Nuevas payloads: 2
- ✅ Nuevos tipos de notificación: 1
- ✅ Nuevos endpoints API: 1
- ✅ Nuevos métodos en repository: 1
- ✅ Nuevos métodos en ViewModel: 4
- ✅ Nuevos StateFlows: 2
- ✅ Nuevas composables: 2
- ✅ Nuevas validaciones UI: 2

### Modificadas (cambios existentes)
- ✅ Data class EventDetailUiState.Data: +1 campo
- ✅ Método buildUiState: +1 parámetro
- ✅ EventDetailScreen: +8 LaunchedEffects y variables
- ✅ Socket listener en init: +2 condiciones

---

## 🧪 Verificación de compilación

El código está listo para compilar:
- ✅ Todas las importaciones están presentes
- ✅ Todos los tipos están definidos
- ✅ Todos los métodos están implementados
- ✅ La sintaxis de Kotlin es válida

**Nota:** Existen errores pre-existentes en FriendsScreen.kt que no están relacionados con estos cambios.

---

## 🔄 Dependencias entre cambios

```
SocketEvent.kt (tipos + payloads)
    ↓
EventDetailViewModel.kt (escucha eventos)
    ↓
EventDetailScreen.kt (muestra UI, botones)
    
Models.kt (enum de notificación)
    ↓
(Backend: crea notificaciones)

EventsApi.kt (endpoint leave)
    ↓
EventRepositoryImpl.kt (implementa leave)
    ↓
Repositories.kt (interfaz leave)
    ↓
EventDetailViewModel.kt (usa leave)
    ↓
EventDetailScreen.kt (botón Leave)
```

---

## 📝 Próximos cambios si es necesario

Si necesitas agregar más funcionalidades:

1. **Confirmación con más detalles:**
   - Pasar parámetros adicionales a los diálogos

2. **Animaciones:**
   - Agregar transiciones en los diálogos
   - Animar los botones

3. **Estados intermedios:**
   - Agregar más estados en `EventActionState`
   - Mostrar progress indicators más detallados

4. **Validaciones:**
   - Agregar validaciones adicionales antes de llamar a los endpoints
   - Mostrar warnings si el evento ya está en cierto estado


