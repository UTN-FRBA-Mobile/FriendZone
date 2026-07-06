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

    @Test
    fun isPastEvent_cancelledOrCompleted_isAlwaysPast() {
        val cancelled = testEvent(startsAt = now, status = EventStatus.CANCELLED)
        val completed = testEvent(startsAt = now, status = EventStatus.COMPLETED)

        assertTrue(cancelled.isPastEvent(pastThresholdHours = 48, now = now))
        assertTrue(completed.isPastEvent(pastThresholdHours = 48, now = now))
    }

    @Test
    fun isPastEvent_beforeStart_isNotPast() {
        val event = testEvent(startsAt = now.plus(2, ChronoUnit.HOURS))

        assertEquals(false, event.isPastEvent(pastThresholdHours = 48, now = now))
    }

    @Test
    fun isPastEvent_afterThreshold_isPast() {
        val event = testEvent(
            startsAt = now.minus(50, ChronoUnit.HOURS),
            status = EventStatus.SCHEDULED,
        )

        assertTrue(event.isPastEvent(pastThresholdHours = 48, now = now))
    }

    @Test
    fun isLive_activeStatus_isLiveRegardlessOfTime() {
        val futureEvent = testEvent(
            startsAt = now.plus(5, ChronoUnit.HOURS),
            status = EventStatus.ACTIVE,
        )

        assertTrue(futureEvent.isLive(now = now))
    }

    @Test
    fun isLive_terminalStatus_isNotLive() {
        val completed = testEvent(startsAt = now, status = EventStatus.COMPLETED)
        val cancelled = testEvent(startsAt = now, status = EventStatus.CANCELLED)

        assertEquals(false, completed.isLive(now = now))
        assertEquals(false, cancelled.isLive(now = now))
    }

    @Test
    fun isTrackingOpen_beforeLeadWindow_isClosed() {
        val event = testEvent(
            startsAt = now.plus(60, ChronoUnit.MINUTES),
            trackingLeadMinutes = 30,
        )

        assertEquals(false, event.isTrackingOpen(now = now))
    }

    @Test
    fun isTrackingOpen_insideLeadWindow_isOpen() {
        val event = testEvent(
            startsAt = now.plus(15, ChronoUnit.MINUTES),
            trackingLeadMinutes = 30,
        )

        assertTrue(event.isTrackingOpen(now = now))
    }

    @Test
    fun canPromptOrganizerToComplete_startedWithGuests_canPrompt() {
        val event = testEvent(
            startsAt = now.minus(10, ChronoUnit.MINUTES),
            status = EventStatus.ACTIVE,
        )

        assertTrue(event.canPromptOrganizerToComplete(acceptedGuestCount = 2))
    }

    @Test
    fun canPromptOrganizerToComplete_noGuests_cannotPrompt() {
        val event = testEvent(
            startsAt = now.minus(10, ChronoUnit.MINUTES),
            status = EventStatus.ACTIVE,
        )

        assertEquals(false, event.canPromptOrganizerToComplete(acceptedGuestCount = 0))
    }

    @Test
    fun haversineMeters_samePoint_isZero() {
        assertEquals(0.0, haversineMeters(40.0, -74.0, 40.0, -74.0), 0.001)
    }

    @Test
    fun formatRelativeTimeLabel_today_returnsTodayLabel() {
        val startsAt = now.toString()
        val (icon, label) = formatRelativeTimeLabel(startsAt, now = now)

        assertEquals("🟢", icon)
        assertEquals("Today", label)
    }

    @Test
    fun formatRelativeTimeLabel_tomorrow_returnsTomorrowLabel() {
        val startsAt = now.plus(1, ChronoUnit.DAYS).toString()
        val (icon, label) = formatRelativeTimeLabel(startsAt, now = now)

        assertEquals("🕐", icon)
        assertEquals("Tomorrow", label)
    }

    @Test
    fun formatRelativeTimeLabel_inRangeDays_returnsInDaysLabel() {
        val startsAt = now.plus(3, ChronoUnit.DAYS).toString()
        val (icon, label) = formatRelativeTimeLabel(startsAt, now = now)

        assertEquals("🕐", icon)
        assertEquals("In 3 days", label)
    }

    @Test
    fun formatRelativeTimeLabel_yesterday_returnsYesterdayLabel() {
        val startsAt = now.minus(1, ChronoUnit.DAYS).toString()
        val (icon, label) = formatRelativeTimeLabel(startsAt, now = now)

        assertEquals("📅", icon)
        assertEquals("Yesterday", label)
    }

    @Test
    fun formatRelativeTimeLabel_pastDays_returnsDaysAgoLabel() {
        val startsAt = now.minus(4, ChronoUnit.DAYS).toString()
        val (icon, label) = formatRelativeTimeLabel(startsAt, now = now)

        assertEquals("📅", icon)
        assertEquals("4 days ago", label)
    }

    @Test
    fun formatRelativeTimeLabel_nextWeek_returnsNextWeekLabel() {
        val startsAt = now.plus(10, ChronoUnit.DAYS).toString()
        val (icon, label) = formatRelativeTimeLabel(startsAt, now = now)

        assertEquals("📅", icon)
        assertEquals("Next week", label)
    }

    private fun testEvent(
        startsAt: Instant,
        status: EventStatus = EventStatus.SCHEDULED,
        trackingLeadMinutes: Int = 30,
    ): Event = Event(
        id = "event-1",
        organizerId = "org-1",
        title = "Test Event",
        description = null,
        latitude = 0.0,
        longitude = 0.0,
        address = null,
        status = status,
        arrivalThresholdM = 100,
        trackingLeadMinutes = trackingLeadMinutes,
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
