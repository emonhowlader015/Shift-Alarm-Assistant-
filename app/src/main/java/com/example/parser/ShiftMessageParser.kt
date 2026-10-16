package com.example.parser

import com.example.model.ParseDiscrepancy
import com.example.model.ShiftParseResult
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import java.util.regex.Pattern

data class ParsedTimeRange(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val rawText: String,
    val is12Hour: Boolean
) {
    val startTimeIso: String = String.format(Locale.US, "%02d:%02d", startHour, startMinute)
    val endTimeIso: String = String.format(Locale.US, "%02d:%02d", endHour, endMinute)

    val displayString: String
        get() {
            fun formatPart(hour: Int, minute: Int): String {
                val amPm = if (hour < 12) "AM" else "PM"
                val h = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                return if (minute == 0) "$h:00 $amPm" else String.format(Locale.US, "%d:%02d %am", h, minute, amPm)
            }
            return "${formatPart(startHour, startMinute)} - ${formatPart(endHour, endMinute)}"
        }
}

object ShiftMessageParser {

    // Regex for matching times like "12:00 PM", "12:00pm", "7:00 AM", "6 AM", "14:00", "22:30"
    private const val TIME_PART = """(?:\d{1,2}(?::\d{2})?\s*(?:AM|PM|am|pm)|\d{1,2}:\d{2})"""
    private const val SEPARATOR = """(?:\s*(?:-|–|—|to|until)\s*)"""
    private val TIME_RANGE_REGEX = Pattern.compile(
        """($TIME_PART)$SEPARATOR($TIME_PART)""",
        Pattern.CASE_INSENSITIVE
    )

    // Greeting regex for extracting employee names
    // Supports: "Dear Emon,", "Dear Rahim Ahmed,", "Hi Sarah Jenkins,", "Hello Alex,", "Employee: John Doe"
    private val GREETING_REGEX = Pattern.compile(
        """(?:Dear|Hello|Hi|Greetings|Attention|For|Employee:?)\s+([A-Z][a-zA-Z'\-]+(?:\s+[A-Z][a-zA-Z'\-]+)*)(?:,|\.|\s+-|\s+your|\s+here)""",
        Pattern.CASE_INSENSITIVE
    )

    // Department/Role regex e.g. "- Cutting Edger, Production"
    private val ROLE_REGEX = Pattern.compile(
        """(?:-\s*|\|\s*)([A-Za-z\s]+(?:,\s*[A-Za-z\s]+)*)\s*$""",
        Pattern.MULTILINE
    )

    /**
     * Parses email subject and body into a structured ShiftParseResult.
     */
    fun parse(
        subject: String,
        body: String,
        currentDateTime: LocalDateTime = LocalDateTime.now(),
        configuredUserFilterName: String? = null
    ): ShiftParseResult {
        val discrepancies = mutableListOf<ParseDiscrepancy>()

        // 1. Extract times from subject and body
        val subjectTimes = extractTimeRange(subject)
        val bodyTimes = extractTimeRange(body)

        val resolvedTimes: ParsedTimeRange?
        if (subjectTimes != null && bodyTimes != null) {
            // Check for conflict between subject and body
            if (subjectTimes.startTimeIso != bodyTimes.startTimeIso || subjectTimes.endTimeIso != bodyTimes.endTimeIso) {
                discrepancies.add(
                    ParseDiscrepancy(
                        isSubjectBodyTimeConflict = true,
                        details = "Subject specifies (${subjectTimes.displayString}) while Body specifies (${bodyTimes.displayString})"
                    )
                )
                // When in conflict, body is usually more specific with exact date, but we flag conflict
                resolvedTimes = subjectTimes
            } else {
                resolvedTimes = subjectTimes
            }
        } else {
            resolvedTimes = subjectTimes ?: bodyTimes
        }

        // 2. Extract employee name
        val employeeName = extractEmployeeName(body) ?: extractEmployeeName(subject)

        // 3. Extract date
        val (parsedDate, parsedWeekday) = extractDate(body.ifEmpty { subject })

        // 4. Validate weekday against actual calendar date
        if (parsedDate != null && parsedWeekday != null) {
            val actualDayOfWeek = parsedDate.dayOfWeek
            if (!isMatchingDayOfWeek(actualDayOfWeek, parsedWeekday)) {
                discrepancies.add(
                    ParseDiscrepancy(
                        isWeekdayMismatch = true,
                        details = "Stated weekday '$parsedWeekday' does not match calendar date $parsedDate which is ${actualDayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}"
                    )
                )
            }
        }

        // 5. Validate whether shift is in the past
        if (parsedDate != null && resolvedTimes != null) {
            val shiftStartDateTime = LocalDateTime.of(
                parsedDate,
                LocalTime.of(resolvedTimes.startHour, resolvedTimes.startMinute)
            )
            if (shiftStartDateTime.isBefore(currentDateTime)) {
                discrepancies.add(
                    ParseDiscrepancy(
                        isPastShift = true,
                        details = "Shift was scheduled for ${shiftStartDateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))}, which is already in the past"
                    )
                )
            }
        }

        // 6. Extract department/role
        val department = extractDepartment(body)

        // Calculate confidence
        var confidence = 0.0f
        if (resolvedTimes != null) confidence += 0.4f
        if (parsedDate != null) confidence += 0.35f
        if (employeeName != null) confidence += 0.25f
        if (discrepancies.isNotEmpty()) {
            confidence -= (discrepancies.size * 0.2f).coerceAtMost(0.5f)
        }

        val isSuccess = resolvedTimes != null && parsedDate != null

        return ShiftParseResult(
            isSuccessful = isSuccess,
            employeeName = employeeName,
            shiftDateIso = parsedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            startTimeIso = resolvedTimes?.startTimeIso,
            endTimeIso = resolvedTimes?.endTimeIso,
            displayTimeRange = resolvedTimes?.displayString,
            department = department,
            discrepancies = discrepancies,
            confidenceScore = confidence.coerceIn(0f, 1f),
            rawSubject = subject,
            rawBody = body
        )
    }

    /**
     * Extracts time range e.g. "12:00 PM - 8:00 PM", "7:00 AM - 3:00 PM", "14:00 - 22:00"
     */
    fun extractTimeRange(text: String): ParsedTimeRange? {
        if (text.isBlank()) return null
        val matcher = TIME_RANGE_REGEX.matcher(text)
        if (matcher.find()) {
            val startRaw = matcher.group(1)?.trim() ?: return null
            val endRaw = matcher.group(2)?.trim() ?: return null

            val startParsed = parseSingleTime(startRaw)
            val endParsed = parseSingleTime(endRaw)

            if (startParsed != null && endParsed != null) {
                return ParsedTimeRange(
                    startHour = startParsed.first,
                    startMinute = startParsed.second,
                    endHour = endParsed.first,
                    endMinute = endParsed.second,
                    rawText = matcher.group(0) ?: "$startRaw - $endRaw",
                    is12Hour = startRaw.contains("AM", ignoreCase = true) || startRaw.contains("PM", ignoreCase = true)
                )
            }
        }
        return null
    }

    private fun parseSingleTime(raw: String): Pair<Int, Int>? {
        val trimmed = raw.trim().replace("\\s+".toRegex(), " ")
        val isPm = trimmed.endsWith("PM", ignoreCase = true)
        val isAm = trimmed.endsWith("AM", ignoreCase = true)
        val cleanTime = trimmed.replace("(?i)(AM|PM)".toRegex(), "").trim()

        val parts = cleanTime.split(":")
        val rawHour = parts.getOrNull(0)?.toIntOrNull() ?: return null
        val minute = if (parts.size > 1) parts[1].toIntOrNull() ?: 0 else 0

        var hour = rawHour
        if (isPm) {
            if (hour < 12) hour += 12
        } else if (isAm) {
            if (hour == 12) hour = 0
        }
        if (hour in 0..23 && minute in 0..59) {
            return Pair(hour, minute)
        }
        return null
    }

    /**
     * Extracts employee name dynamically from greetings.
     */
    fun extractEmployeeName(text: String): String? {
        if (text.isBlank()) return null
        val matcher = GREETING_REGEX.matcher(text)
        if (matcher.find()) {
            val name = matcher.group(1)?.trim()
            if (!name.isNullOrBlank() && name.length in 2..50) {
                // Ensure name isn't a common greeting word
                val blocked = listOf("all", "team", "everyone", "staff", "employee", "worker")
                if (!blocked.contains(name.lowercase())) {
                    return name
                }
            }
        }
        // Fallback for "Shift for [Name]:" or "Name: [Name]"
        val namePattern = Pattern.compile("""(?:Name|Shift for|Employee)\s*:\s*([A-Z][a-zA-Z'\-]+(?:\s+[A-Z][a-zA-Z'\-]+)*)""")
        val m2 = namePattern.matcher(text)
        if (m2.find()) {
            return m2.group(1)?.trim()
        }
        return null
    }

    /**
     * Extracts date and optional stated weekday from message text.
     * Supports:
     * - DD.MM.YYYY (e.g. 12.10.2026)
     * - DD/MM/YYYY (e.g. 12/10/2026)
     * - YYYY-MM-DD (e.g. 2026-10-12)
     * - October 12, 2026 / 12 October 2026 / Oct 12, 2026 / 12 Oct 2026
     */
    fun extractDate(text: String): Pair<LocalDate?, String?> {
        if (text.isBlank()) return Pair(null, null)

        // Stated weekday matcher e.g. "Monday", "Mon", "Thursday"
        val weekdayPattern = Pattern.compile(
            """\b(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday|Mon|Tue|Wed|Thu|Fri|Sat|Sun)\b""",
            Pattern.CASE_INSENSITIVE
        )
        val weekdayMatcher = weekdayPattern.matcher(text)
        val statedWeekday = if (weekdayMatcher.find()) weekdayMatcher.group(1) else null

        // 1. DD.MM.YYYY or DD/MM/YYYY
        val numericDatePattern = Pattern.compile("""\b(\d{1,2})[./\-](\d{1,2})[./\-](\d{4})\b""")
        val numMatcher = numericDatePattern.matcher(text)
        if (numMatcher.find()) {
            val p1 = numMatcher.group(1)?.toIntOrNull() ?: 0
            val p2 = numMatcher.group(2)?.toIntOrNull() ?: 0
            val year = numMatcher.group(3)?.toIntOrNull() ?: 0

            // Usually DD.MM.YYYY in international context (e.g. 12.10.2026)
            var date = tryCreateDate(year, p2, p1) // Day=p1, Month=p2
            if (date == null && p1 in 1..12) {
                // Try MM/DD/YYYY if DD/MM was invalid
                date = tryCreateDate(year, p1, p2)
            }
            if (date != null) {
                return Pair(date, statedWeekday)
            }
        }

        // 2. YYYY-MM-DD
        val isoPattern = Pattern.compile("""\b(\d{4})-(\d{1,2})-(\d{1,2})\b""")
        val isoMatcher = isoPattern.matcher(text)
        if (isoMatcher.find()) {
            val y = isoMatcher.group(1)?.toIntOrNull() ?: 0
            val m = isoMatcher.group(2)?.toIntOrNull() ?: 0
            val d = isoMatcher.group(3)?.toIntOrNull() ?: 0
            val date = tryCreateDate(y, m, d)
            if (date != null) return Pair(date, statedWeekday)
        }

        // 3. Named month format: "October 12, 2026" or "12 October 2026" or "Oct 12, 2026"
        val monthsRegex = "(?:January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)"
        val namedMonthPattern1 = Pattern.compile("""\b($monthsRegex)\s+(\d{1,2})(?:st|nd|rd|th)?,?\s+(\d{4})\b""", Pattern.CASE_INSENSITIVE)
        val mNamed1 = namedMonthPattern1.matcher(text)
        if (mNamed1.find()) {
            val monthStr = mNamed1.group(1) ?: ""
            val day = mNamed1.group(2)?.toIntOrNull() ?: 0
            val year = mNamed1.group(3)?.toIntOrNull() ?: 0
            val month = parseMonthName(monthStr)
            if (month != null) {
                val date = tryCreateDate(year, month, day)
                if (date != null) return Pair(date, statedWeekday)
            }
        }

        val namedMonthPattern2 = Pattern.compile("""\b(\d{1,2})(?:st|nd|rd|th)?\s+($monthsRegex),?\s+(\d{4})\b""", Pattern.CASE_INSENSITIVE)
        val mNamed2 = namedMonthPattern2.matcher(text)
        if (mNamed2.find()) {
            val day = mNamed2.group(1)?.toIntOrNull() ?: 0
            val monthStr = mNamed2.group(2) ?: ""
            val year = mNamed2.group(3)?.toIntOrNull() ?: 0
            val month = parseMonthName(monthStr)
            if (month != null) {
                val date = tryCreateDate(year, month, day)
                if (date != null) return Pair(date, statedWeekday)
            }
        }

        return Pair(null, statedWeekday)
    }

    private fun tryCreateDate(year: Int, month: Int, day: Int): LocalDate? {
        return try {
            if (year in 2000..2100 && month in 1..12 && day in 1..31) {
                LocalDate.of(year, month, day)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseMonthName(name: String): Int? {
        val lower = name.lowercase()
        return when {
            lower.startsWith("jan") -> 1
            lower.startsWith("feb") -> 2
            lower.startsWith("mar") -> 3
            lower.startsWith("apr") -> 4
            lower.startsWith("may") -> 5
            lower.startsWith("jun") -> 6
            lower.startsWith("jul") -> 7
            lower.startsWith("aug") -> 8
            lower.startsWith("sep") -> 9
            lower.startsWith("oct") -> 10
            lower.startsWith("nov") -> 11
            lower.startsWith("dec") -> 12
            else -> null
        }
    }

    private fun isMatchingDayOfWeek(dayOfWeek: DayOfWeek, statedWeekday: String): Boolean {
        val target = dayOfWeek.name.lowercase()
        val stated = statedWeekday.lowercase()
        return target.startsWith(stated) || stated.startsWith(target.take(3))
    }

    /**
     * Extracts department or role e.g. "Cutting Edger, Production"
     */
    fun extractDepartment(body: String): String? {
        val matcher = ROLE_REGEX.matcher(body)
        if (matcher.find()) {
            val role = matcher.group(1)?.trim()
            if (!role.isNullOrBlank() && role.length in 3..60) {
                return role
            }
        }
        return null
    }

    /**
     * Computes the exact reminder trigger time in milliseconds.
     * Alarm DateTime = Shift Start DateTime - Reminder Duration.
     */
    fun calculateReminderEpochMillis(
        dateIso: String,
        startTimeIso: String,
        reminderMinutesBefore: Int = 120,
        zoneId: java.time.ZoneId = java.time.ZoneId.systemDefault()
    ): Long {
        val date = LocalDate.parse(dateIso)
        val timeParts = startTimeIso.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts[1].toInt()
        val shiftStart = LocalDateTime.of(date, LocalTime.of(hour, minute))
        val reminderDateTime = shiftStart.minusMinutes(reminderMinutesBefore.toLong())
        return reminderDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }
}
