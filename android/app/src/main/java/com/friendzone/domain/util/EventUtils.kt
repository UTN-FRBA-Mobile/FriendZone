package com.example.friendzone.domain.util

import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventParticipant
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.ParticipantWithUser
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_M = 6_371_000.0
private const val ASSUMED_SPEED_MPS = 8.33 // ~30 km/h

sealed class ParticipantStatus {
    data object Arrived : ParticipantStatus()
    data class InTransit(val etaMinutes: Int?) : ParticipantStatus()
    data class Delayed(val etaMinutes: Int?) : ParticipantStatus()
}

fun Event.parseStartsAt(): Instant = Instant.parse(startsAt)

fun Event.hasStarted(now: Instant = Instant.now()): Boolean = !now.isBefore(parseStartsAt())

fun Event.canPromptOrganizerToComplete(acceptedGuestCount: Int): Boolean =
    hasStarted() &&
        status != EventStatus.COMPLETED &&
        status != EventStatus.CANCELLED &&
        acceptedGuestCount >= 1

fun Event.isLive(now: Instant = Instant.now()): Boolean {
    if (status == EventStatus.COMPLETED || status == EventStatus.CANCELLED) return false
    if (status == EventStatus.ACTIVE) return true
    return parseStartsAt() <= now
}

fun Event.isPastEvent(
    pastThresholdHours: Int,
    now: Instant = Instant.now(),
): Boolean {
    val startsAt = parseStartsAt()
    val thresholdHours = pastThresholdHours.coerceAtLeast(0)

    when (status) {
        EventStatus.CANCELLED -> return true
        EventStatus.COMPLETED -> return true
        else -> {
            if (now.isBefore(startsAt)) return false
            val pastAt = startsAt.plus(thresholdHours.toLong(), ChronoUnit.HOURS)
            return !now.isBefore(pastAt)
        }
    }
}

fun Event.isTrackingOpen(now: Instant = Instant.now()): Boolean {
    if (status == EventStatus.COMPLETED || status == EventStatus.CANCELLED) return false
    val trackingStartsAt = parseStartsAt().minusSeconds(trackingLeadMinutes.toLong() * 60)
    return now >= trackingStartsAt
}

fun haversineMeters(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
): Double {
    val toRad = { deg: Double -> Math.toRadians(deg) }
    val dLat = toRad(lat2 - lat1)
    val dLon = toRad(lng2 - lng1)
    val rLat1 = toRad(lat1)
    val rLat2 = toRad(lat2)
    val sinDLat = sin(dLat / 2)
    val sinDLon = sin(dLon / 2)
    val h = sinDLat.pow(2) + cos(rLat1) * cos(rLat2) * sinDLon.pow(2)
    return 2 * EARTH_RADIUS_M * asin(sqrt(h))
}

fun estimateMinutesAway(
    participant: EventParticipant,
    event: Event,
): Int? {
    val lat = participant.lastLatitude ?: return null
    val lng = participant.lastLongitude ?: return null
    val distanceM = haversineMeters(lat, lng, event.latitude, event.longitude)
    return kotlin.math.ceil(distanceM / ASSUMED_SPEED_MPS / 60.0).toInt().coerceAtLeast(1)
}

fun minutesUntilStart(event: Event, now: Instant = Instant.now()): Int {
    val startsAt = event.parseStartsAt()
    if (!startsAt.isAfter(now)) return 0
    return Duration.between(now, startsAt).toMinutes().toInt().coerceAtLeast(0)
}

fun classifyParticipant(
    participant: EventParticipant,
    event: Event,
    now: Instant = Instant.now(),
): ParticipantStatus {
    if (participant.arrived) return ParticipantStatus.Arrived

    val eventStarted = event.hasStarted(now)
    val untilStart = minutesUntilStart(event, now)
    val eta = estimateMinutesAway(participant, event)

    if (eta == null) {
        return if (eventStarted) {
            ParticipantStatus.Delayed(null)
        } else {
            ParticipantStatus.InTransit(null)
        }
    }

    return if (eventStarted || eta > untilStart) {
        ParticipantStatus.Delayed(eta)
    } else {
        ParticipantStatus.InTransit(eta)
    }
}

fun classifyParticipantWithUser(
    item: ParticipantWithUser,
    event: Event,
    now: Instant = Instant.now(),
): ParticipantStatus = classifyParticipant(item.participant, event, now)

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, h:mm a")
    .withZone(ZoneId.systemDefault())

fun formatEventDate(startsAt: String): String = dateFormatter.format(Instant.parse(startsAt))

sealed class RelativeTimeLabel {
    data object Today : RelativeTimeLabel()
    data object Tomorrow : RelativeTimeLabel()
    data object Yesterday : RelativeTimeLabel()
    data class InDays(val days: Long) : RelativeTimeLabel()
    data class DaysAgo(val days: Long) : RelativeTimeLabel()
    data object NextWeek : RelativeTimeLabel()
}

fun getRelativeTimeLabel(startsAt: String, now: Instant = Instant.now()): Pair<String, RelativeTimeLabel> {
    val instant = Instant.parse(startsAt)
    val zone = ZoneId.systemDefault()
    val eventDate = instant.atZone(zone).toLocalDate()
    val today = now.atZone(zone).toLocalDate()
    val daysBetween = ChronoUnit.DAYS.between(today, eventDate)

    val icon = when {
        daysBetween == 0L -> "🟢"
        daysBetween in 1L..6L -> "🕐"
        else -> "📅"
    }
    val label = when {
        daysBetween == 0L -> RelativeTimeLabel.Today
        daysBetween == 1L -> RelativeTimeLabel.Tomorrow
        daysBetween == -1L -> RelativeTimeLabel.Yesterday
        daysBetween in 2L..6L -> RelativeTimeLabel.InDays(daysBetween)
        daysBetween < -1L -> RelativeTimeLabel.DaysAgo(-daysBetween)
        else -> RelativeTimeLabel.NextWeek
    }
    return icon to label
}
