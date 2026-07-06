package com.example.friendzone.data.location

import android.content.Context
import android.location.Address
import android.location.Geocoder
import com.example.friendzone.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/** Resultado de geocodificar una direccion: coordenadas + texto legible. */
data class GeoPlace(
    val latitude: Double,
    val longitude: Double,
    val address: String,
)

/**
 * Conversion entre direcciones y coordenadas usando el [Geocoder] del
 * framework (sin Google Play Services). Depende de que el dispositivo tenga un
 * backend de geocoding: algunos emuladores sin Google APIs no lo traen.
 */
@Singleton
class GeocoderHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val geocoder by lazy { Geocoder(context, Locale.getDefault()) }

    /** Coordenadas -> direccion legible. Null si no se puede resolver. */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? {
        if (!Geocoder.isPresent()) return null
        return withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resumeWith(Result.success(addresses.firstOrNull()?.let(::formatAddress)))
                    }

                    override fun onError(errorMessage: String?) {
                        cont.resumeWith(Result.success(null))
                    }
                })
            }
        }
    }

    /** Direccion escrita -> coordenadas + direccion normalizada. Null si no la encuentra. */
    suspend fun forwardGeocode(query: String): GeoPlace? {
        if (!Geocoder.isPresent() || query.isBlank()) return null
        return withTimeoutOrNull(TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val place = addresses.firstOrNull()?.let { address ->
                            GeoPlace(address.latitude, address.longitude, formatAddress(address))
                        }
                        cont.resumeWith(Result.success(place))
                    }

                    override fun onError(errorMessage: String?) {
                        cont.resumeWith(Result.success(null))
                    }
                })
            }
        }
    }

    private fun formatAddress(address: Address): String {
        if (address.maxAddressLineIndex >= 0) {
            return address.getAddressLine(0)
        }
        return listOfNotNull(
            address.thoroughfare,
            address.subThoroughfare,
            address.locality,
            address.adminArea,
        ).joinToString(", ").ifBlank { context.getString(R.string.label_unknown_location) }
    }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
