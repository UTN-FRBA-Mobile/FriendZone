package com.example.friendzone.domain.util

import com.example.friendzone.domain.model.Event
import com.example.friendzone.domain.model.EventParticipant
import com.example.friendzone.domain.model.EventStatus
import com.example.friendzone.domain.model.ParticipantRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventUtilsTest {
    private val now = Instant.parse("2026-07-05T18:00:00Z")

    @Test
    fun classifyParticipant_arrived_returnsArrived() {
        val event = testEvent(startsAt = now.plus(30, ChronoUnit.MINUTES))
        val participant = testParticipant(arrived = true)

        assertEquals(ParticipantStatus.Arrived, classifyParticipant(participant, event, now))
    }

    @Test
    fun classifyParticipant_preStart_etaBeforeStart_isInTransit() {
        val event = testEvent(startsAt = now.plus(20, ChronoUnit.MINUTES))
        val participant = testParticipantAtEtaMinutes(event, etaMinutes = 10)

        val status = classifyParticipant(participant, event, now)
        assertTrue(status is ParticipantStatus.InTransit)
        assertEquals(10, (status as ParticipantStatus.InTransit).etaMinutes)
    }

    @Test
    fun classifyParticipant_preStart_etaEqualsStart_isInTransit() {
        val event = testEvent(startsAt = now.plus(20, ChronoUnit.MINUTES))
        val participant = testParticipantAtEtaMinutes(event, etaMinutes = 20)

        val status = classifyParticipant(participant, event, now)
        assertTrue(status is ParticipantStatus.InTransit)
        assertEquals(20, (status as ParticipantStatus.InTransit).etaMinutes)
    }

    @Test
    fun classifyParticipant_preStart_etaAfterStart_isDelayed() {
        val event = testEvent(startsAt = now.plus(20, ChronoUnit.MINUTES))
        val participant = testParticipantAtEtaMinutes(event, etaMinutes = 25)

        val status = classifyParticipant(participant, event, now)
        assertTrue(status is ParticipantStatus.Delayed)
        assertEquals(25, (status as ParticipantStatus.Delayed).etaMinutes)
    }

    @Test
    fun classifyParticipant_postStart_notArrived_isDelayedWithEta() {
        val event = testEvent(startsAt = now.minus(5, ChronoUnit.MINUTES))
        val participant = testParticipantAtEtaMinutes(event, etaMinutes = 5)

        val status = classifyParticipant(participant, event, now)
        assertTrue(status is ParticipantStatus.Delayed)
        assertEquals(5, (status as ParticipantStatus.Delayed).etaMinutes)
    }

    @Test
    fun classifyParticipant_postStart_noCoords_isDelayedWithoutEta() {
        val event = testEvent(startsAt = now.minus(5, ChronoUnit.MINUTES))
        val participant = testParticipant()

        val status = classifyParticipant(participant, event, now)
        assertTrue(status is ParticipantStatus.Delayed)
        assertEquals(null, (status as ParticipantStatus.Delayed).etaMinutes)
    }

    @Test
    fun classifyParticipant_preStart_noCoords_isInTransitWithoutEta() {
        val event = testEvent(startsAt = now.plus(20, ChronoUnit.MINUTES))
        val participant = testParticipant()

        val status = classifyParticipant(participant, event, now)
        assertTrue(status is ParticipantStatus.InTransit)
        assertEquals(null, (status as ParticipantStatus.InTransit).etaMinutes)
    }

    @Test
    fun travelEtaSubtitle_withEta_showsMinutesAway() {
        val status = ParticipantStatus.Delayed(12)

        assertEquals("12 min away", status.travelEtaSubtitle())
    }

    @Test
    fun travelEtaSubtitle_withoutEta_showsUnavailable() {
        val status = ParticipantStatus.Delayed(null)

        assertEquals("Arrival time unavailable", status.travelEtaSubtitle())
    }

    @Test
    fun statusPillText_matchesStatusLabel() {
        assertEquals("✓ Arrived", ParticipantStatus.Arrived.statusPillText())
        assertEquals("In Transit", ParticipantStatus.InTransit(10).statusPillText())
        assertEquals("Delayed", ParticipantStatus.Delayed(10).statusPillText())
    }

    private fun testEvent(startsAt: Instant): Event = Event(
        id = "event-1",
        organizerId = "org-1",
        title = "Test Event",
        description = null,
        latitude = 0.0,
        longitude = 0.0,
        address = null,
        status = EventStatus.SCHEDULED,
        arrivalThresholdM = 100,
        trackingLeadMinutes = 30,
        startsAt = startsAt.toString(),
        completedAt = null,
        createdAt = now.toString(),
    )

    private fun testParticipant(
        arrived: Boolean = false,
        lastLatitude: Double? = null,
        lastLongitude: Double? = null,
    ): EventParticipant = EventParticipant(
        id = "participant-1",
        eventId = "event-1",
        userId = "user-1",
        role = ParticipantRole.PARTICIPANT,
        sharingLocation = true,
        arrived = arrived,
        lastLatitude = lastLatitude,
        lastLongitude = lastLongitude,
        lastLocationAt = null,
        arrivedAt = null,
        createdAt = now.toString(),
    )

    private fun testParticipantAtEtaMinutes(event: Event, etaMinutes: Int): EventParticipant {
        var low = 0.0
        var high = 5.0
        var bestParticipant = testParticipant(
            lastLatitude = event.latitude + high,
            lastLongitude = event.longitude,
        )
        repeat(40) {
            val mid = (low + high) / 2.0
            val candidate = testParticipant(
                lastLatitude = event.latitude + mid,
                lastLongitude = event.longitude,
            )
            val estimated = estimateMinutesAway(candidate, event) ?: 0
            bestParticipant = candidate
            when {
                estimated < etaMinutes -> low = mid
                estimated > etaMinutes -> high = mid
                else -> return candidate
            }
        }
        return bestParticipant
    }
}
