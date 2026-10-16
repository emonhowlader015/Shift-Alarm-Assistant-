package com.example

import com.example.parser.ShiftMessageParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ShiftMessageParserTest {

    @Test
    fun testDynamicEmployeeNameExtraction() {
        val names = listOf(
            "Dear Emon, your work shift has been re-scheduled" to "Emon",
            "Dear Rahim Ahmed, your work shift has been re-scheduled" to "Rahim Ahmed",
            "Dear Sarah Jenkins, your work shift has been re-scheduled" to "Sarah Jenkins",
            "Hello Alex, here is your updated schedule" to "Alex",
            "Hi Jordan-Taylor, your shift is ready" to "Jordan-Taylor",
            "Employee: Marcus Aurelius" to "Marcus Aurelius"
        )

        for ((text, expectedName) in names) {
            val extracted = ShiftMessageParser.extractEmployeeName(text)
            assertEquals("Failed extracting name for: $text", expectedName, extracted)
        }
    }

    @Test
    fun testVariousShiftTimesAndFormats() {
        // 12-hour with minutes
        val r1 = ShiftMessageParser.extractTimeRange("Shift Re-scheduled: 12:00 PM - 8:00 PM")
        assertNotNull(r1)
        assertEquals(12, r1!!.startHour)
        assertEquals(0, r1.startMinute)
        assertEquals(20, r1.endHour)
        assertEquals(0, r1.endMinute)

        // Morning shift
        val r2 = ShiftMessageParser.extractTimeRange("Shift Re-scheduled: 7:00 AM - 3:00 PM")
        assertNotNull(r2)
        assertEquals(7, r2!!.startHour)
        assertEquals(15, r2.endHour)

        // 24-hour format
        val r3 = ShiftMessageParser.extractTimeRange("Shift Re-scheduled: 14:00 - 22:00")
        assertNotNull(r3)
        assertEquals(14, r3!!.startHour)
        assertEquals(22, r3.endHour)

        // No-minute format
        val r4 = ShiftMessageParser.extractTimeRange("Shift Re-scheduled: 6 AM - 2 PM")
        assertNotNull(r4)
        assertEquals(6, r4!!.startHour)
        assertEquals(14, r4.endHour)

        // Overnight shift
        val r5 = ShiftMessageParser.extractTimeRange("Shift Re-scheduled: 10:00 PM - 6:00 AM")
        assertNotNull(r5)
        assertEquals(22, r5!!.startHour)
        assertEquals(6, r5.endHour)
    }

    @Test
    fun testMultipleDateFormats() {
        val f1 = ShiftMessageParser.extractDate("Shift on 12.10.2026 Monday")
        assertEquals("2026-10-12", f1.first?.toString())
        assertEquals("Monday", f1.second)

        val f2 = ShiftMessageParser.extractDate("Shift on 12/10/2026")
        assertEquals("2026-10-12", f2.first?.toString())

        val f3 = ShiftMessageParser.extractDate("Shift on 2026-10-12")
        assertEquals("2026-10-12", f3.first?.toString())

        val f4 = ShiftMessageParser.extractDate("Shift on October 12, 2026")
        assertEquals("2026-10-12", f4.first?.toString())

        val f5 = ShiftMessageParser.extractDate("Shift on 12 October 2026")
        assertEquals("2026-10-12", f5.first?.toString())
    }

    @Test
    fun testWeekdayMismatchDetection() {
        // 2026-10-12 is actually a Monday. If text claims Wednesday, flag mismatch.
        val result = ShiftMessageParser.parse(
            subject = "Shift Re-scheduled: 12:00 PM - 8:00 PM",
            body = "Dear Jordan, your work shift has been re-scheduled to 12:00 PM - 8:00 PM for 12.10.2026 Wednesday.",
            currentDateTime = LocalDateTime.of(2026, 10, 1, 0, 0)
        )

        assertTrue(result.discrepancies.any { it.isWeekdayMismatch })
    }

    @Test
    fun testSubjectBodyConflictDetection() {
        val result = ShiftMessageParser.parse(
            subject = "Shift Re-scheduled: 7:00 AM - 3:00 PM",
            body = "Dear Emon, your work shift has been re-scheduled to 12:00 PM - 8:00 PM for 12.10.2026 Monday.",
            currentDateTime = LocalDateTime.of(2026, 10, 1, 0, 0)
        )

        assertTrue(result.discrepancies.any { it.isSubjectBodyTimeConflict })
    }

    @Test
    fun testMissingSubjectTimeFallbackToBody() {
        val result = ShiftMessageParser.parse(
            subject = "Shift Re-scheduled",
            body = "Dear Sarah, your work shift has been re-scheduled to 10:00 PM - 6:00 AM for 18.10.2026 Sunday.",
            currentDateTime = LocalDateTime.of(2026, 10, 1, 0, 0)
        )

        assertTrue(result.isSuccessful)
        assertEquals("Sarah", result.employeeName)
        assertEquals("22:00", result.startTimeIso)
        assertEquals("06:00", result.endTimeIso)
        assertEquals("2026-10-18", result.shiftDateIso)
    }

    @Test
    fun testDefaultReminderCalculation() {
        // Shift date: 12 October 2026 at 12:00 PM
        // Reminder: 2 hours (120 minutes) before -> Expect 10:00 AM on 12 October 2026
        val zone = ZoneId.of("UTC")
        val alarmMillis = ShiftMessageParser.calculateReminderEpochMillis(
            dateIso = "2026-10-12",
            startTimeIso = "12:00",
            reminderMinutesBefore = 120,
            zoneId = zone
        )

        val alarmDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarmMillis), zone)
        assertEquals(2026, alarmDt.year)
        assertEquals(10, alarmDt.monthValue)
        assertEquals(12, alarmDt.dayOfMonth)
        assertEquals(10, alarmDt.hour)
        assertEquals(0, alarmDt.minute)
    }

    @Test
    fun testCustomReminderDurationCalculation() {
        // Shift date: 15 October 2026 at 7:00 AM
        // Reminder: 45 minutes before -> Expect 6:15 AM
        val zone = ZoneId.of("UTC")
        val alarmMillis = ShiftMessageParser.calculateReminderEpochMillis(
            dateIso = "2026-10-15",
            startTimeIso = "07:00",
            reminderMinutesBefore = 45,
            zoneId = zone
        )

        val alarmDt = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarmMillis), zone)
        assertEquals(6, alarmDt.hour)
        assertEquals(15, alarmDt.minute)
    }
}
