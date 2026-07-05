package com.example.friendzone.presentation.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.friendzone.ui.theme.FzBorder
import com.example.friendzone.ui.theme.FzGreen
import com.example.friendzone.ui.theme.FzInk
import com.example.friendzone.ui.theme.FzInk3
import com.example.friendzone.ui.theme.FzSurface
import com.example.friendzone.ui.theme.FzSurface2
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

/** Persona (distinta a mi) que comparte su ubicacion en el evento. */
data class EventMapPerson(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val arrived: Boolean,
)

private val EventMarkerColor = Color(0xFF1C6B3A) // verde, igual que FzGreen
private val PersonMarkerColor = Color(0xFFE0772D) // naranja
private val MyMarkerColor = Color(0xFF2D6CDF) // azul (referencia para la leyenda)

/**
 * Miniatura del mapa centrada en la ubicacion del evento. Al tocarla se abre
 * la ventana flotante con el mapa completo.
 */
@Composable
fun EventMapThumbnail(
    eventLatitude: Double,
    eventLongitude: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mapView = rememberMapViewWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, FzBorder, RoundedCornerShape(12.dp)),
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.matchParentSize(),
            update = { map ->
                val point = GeoPoint(eventLatitude, eventLongitude)
                map.controller.setZoom(15.0)
                map.controller.setCenter(point)
                map.overlays.removeAll { it is Marker }
                map.overlays.add(
                    Marker(map).apply {
                        position = point
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        icon = dotDrawable(EventMarkerColor, map.context)
                        setInfoWindow(null)
                    },
                )
                map.invalidate()
            },
        )
        // Captura el tap (y bloquea el scroll/zoom del mapa en la miniatura)
        // para abrir la ventana flotante con el mapa completo.
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable(onClick = onClick),
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(FzInk.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp),
            )
            Text("Ver mapa", color = Color.White, modifier = Modifier)
        }
    }
}

/**
 * Ventana flotante con el mapa completo: mi ubicacion (centrado), la ubicacion
 * del evento y la del resto de las personas que comparten su ubicacion.
 */
@Composable
fun EventMapDialog(
    eventLatitude: Double,
    eventLongitude: Double,
    eventLabel: String,
    people: List<EventMapPerson>,
    isSharingLocation: Boolean,
    onSharingChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    // Si el usuario activa el switch sin permiso, lo pedimos y recordamos que
    // queria compartir para activarlo recien cuando lo concede.
    var pendingEnableSharing by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        val granted = result.values.any { it }
        hasLocationPermission = granted
        if (pendingEnableSharing) {
            pendingEnableSharing = false
            if (granted) onSharingChange(true)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(FzSurface),
        ) {
            EventMapContent(
                eventLatitude = eventLatitude,
                eventLongitude = eventLongitude,
                eventLabel = eventLabel,
                people = people,
                hasLocationPermission = hasLocationPermission,
                modifier = Modifier.matchParentSize(),
            )

            MapLegend(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp),
            )

            // Cruz para cerrar la ventana flotante.
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FzSurface)
                    .border(1.dp, FzBorder, CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = FzInk,
                    modifier = Modifier.size(22.dp),
                )
            }

            ShareLocationToggle(
                checked = isSharingLocation,
                onCheckedChange = { enabled ->
                    if (enabled && !hasLocationPermission) {
                        pendingEnableSharing = true
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    } else {
                        onSharingChange(enabled)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
            )
        }
    }
}

@Composable
private fun ShareLocationToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(FzSurface.copy(alpha = 0.92f))
            .border(1.dp, FzBorder, RoundedCornerShape(14.dp))
            .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Compartir mi ubicacion", color = FzInk)
        FriendZoneSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Ventana flotante para elegir la ubicacion de un evento marcandola en el mapa.
 * El pin queda fijo en el centro: se mueve el mapa para ubicarlo y al confirmar
 * se toma el punto que quedo en el centro. Arranca centrado en mi ubicacion.
 */
@Composable
fun LocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onConfirm: (latitude: Double, longitude: Double) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        hasLocationPermission = result.values.any { it }
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    val mapView = rememberMapViewWithLifecycle()
    val myLocationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context.applicationContext), mapView)
    }
    val hasInitial = initialLatitude != null && initialLongitude != null

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(FzSurface),
        ) {
            AndroidView(
                factory = { mapView },
                modifier = Modifier.matchParentSize(),
                update = {},
            )

            // Pin fijo en el centro de la pantalla (la punta apunta al centro).
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = EventMarkerColor,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
                    .offset(y = (-24).dp),
            )

            Text(
                text = "Move el mapa para ubicar el pin",
                color = FzInk,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(FzSurface.copy(alpha = 0.92f))
                    .border(1.dp, FzBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(FzSurface)
                    .border(1.dp, FzBorder, CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cerrar",
                    tint = FzInk,
                    modifier = Modifier.size(22.dp),
                )
            }

            FriendZonePrimaryButton(
                text = "Confirmar ubicacion",
                onClick = {
                    val center = mapView.mapCenter
                    onConfirm(center.latitude, center.longitude)
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
            )
        }
    }

    LaunchedEffect(mapView) {
        mapView.controller.setZoom(16.0)
        val start = if (hasInitial) {
            GeoPoint(initialLatitude, initialLongitude)
        } else {
            GeoPoint(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        }
        mapView.controller.setCenter(start)
    }

    // Centra el mapa en mi ubicacion al abrir (salvo que ya haya un punto elegido).
    DisposableEffect(mapView, hasLocationPermission) {
        if (hasLocationPermission) {
            if (!mapView.overlays.contains(myLocationOverlay)) {
                mapView.overlays.add(myLocationOverlay)
            }
            myLocationOverlay.enableMyLocation()
            if (!hasInitial) {
                myLocationOverlay.runOnFirstFix {
                    myLocationOverlay.myLocation?.let { loc ->
                        mapView.post {
                            mapView.controller.animateTo(loc)
                            mapView.controller.setZoom(16.0)
                        }
                    }
                }
            }
        }
        onDispose {
            myLocationOverlay.disableMyLocation()
        }
    }
}

// Centro por defecto del selector si no hay permiso ni punto previo (Buenos Aires).
private const val DEFAULT_LATITUDE = -34.6037
private const val DEFAULT_LONGITUDE = -58.3816

@Composable
private fun EventMapContent(
    eventLatitude: Double,
    eventLongitude: Double,
    eventLabel: String,
    people: List<EventMapPerson>,
    hasLocationPermission: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val mapView = rememberMapViewWithLifecycle()
    val myLocationOverlay = remember(mapView) {
        MyLocationNewOverlay(GpsMyLocationProvider(context.applicationContext), mapView)
    }

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.matchParentSize(),
            update = { map ->
                map.overlays.removeAll { it is Marker }
                map.overlays.add(
                    Marker(map).apply {
                        position = GeoPoint(eventLatitude, eventLongitude)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        title = eventLabel
                        icon = dotDrawable(EventMarkerColor, map.context)
                    },
                )
                people.forEach { person ->
                    map.overlays.add(
                        Marker(map).apply {
                            position = GeoPoint(person.latitude, person.longitude)
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            title = if (person.arrived) "${person.label} · llego" else person.label
                            icon = dotDrawable(PersonMarkerColor, map.context)
                        },
                    )
                }
                map.invalidate()
            },
        )

        // Boton para volver a centrar en mi ubicacion.
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(FzSurface)
                .border(1.dp, FzBorder, CircleShape)
                .clickable {
                    val target = myLocationOverlay.myLocation
                        ?: GeoPoint(eventLatitude, eventLongitude)
                    mapView.controller.animateTo(target)
                    mapView.controller.setZoom(16.0)
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Centrar en mi ubicacion",
                tint = FzInk,
                modifier = Modifier.size(24.dp),
            )
        }
    }

    // El mapa siempre arranca centrado en la ubicacion del evento.
    LaunchedEffect(mapView) {
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(GeoPoint(eventLatitude, eventLongitude))
    }

    // Mi ubicacion: muestra el punto azul del GPS (sin mover el mapa). Para
    // centrarte en vos esta el boton de "mi ubicacion".
    DisposableEffect(mapView, hasLocationPermission) {
        if (hasLocationPermission) {
            if (!mapView.overlays.contains(myLocationOverlay)) {
                mapView.overlays.add(myLocationOverlay)
            }
            myLocationOverlay.enableMyLocation()
        }
        onDispose {
            myLocationOverlay.disableMyLocation()
        }
    }
}

@Composable
private fun MapLegend(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(FzSurface.copy(alpha = 0.92f))
            .border(1.dp, FzBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        LegendItem(MyMarkerColor, "Yo")
        LegendItem(EventMarkerColor, "Evento")
        LegendItem(PersonMarkerColor, "Personas")
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, color = FzInk)
    }
}

/**
 * Crea y recuerda un [MapView] de osmdroid atado al ciclo de vida del
 * composable (onResume/onPause/onDetach) para evitar fugas de recursos.
 */
@Composable
private fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            clipToOutline = true
        }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }
    return mapView
}

/** Punto de color (relleno con borde blanco) usado como icono de marcador. */
private fun dotDrawable(color: Color, context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (18 * density).toInt()
    val strokePx = (2 * density).toInt()
    return GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color.toArgb())
        setStroke(strokePx, android.graphics.Color.WHITE)
        setSize(sizePx, sizePx)
    }
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
