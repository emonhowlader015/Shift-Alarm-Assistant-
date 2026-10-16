package com.example.model

enum class ShiftStatus {
    UPCOMING,
    IN_PROGRESS,
    PAST,
    CONFLICT_NEEDS_CONFIRMATION,
    CANCELLED
}

enum class MessageSource {
    GMAIL,
    SMS,
    MANUAL_ENTRY
}

enum class AlarmDestination {
    INTERNAL_ONLY,
    SYSTEM_CLOCK_ONLY,
    BOTH
}

data class Shift(
    val id: Long = 0,
    val employeeName: String,
    val dateString: String, // ISO format: YYYY-MM-DD
    val startTimeString: String, // 24-hr: HH:mm
    val endTimeString: String, // 24-hr: HH:mm
    val displayTimeRange: String, // e.g. "12:00 PM - 8:00 PM"
    val department: String? = null,
    val reminderMinutesBefore: Int = 120, // Default: 2 hours before
    val additionalRemindersMinutes: List<Int> = emptyList(),
    val status: ShiftStatus = ShiftStatus.UPCOMING,
    val source: MessageSource = MessageSource.MANUAL_ENTRY,
    val sourceMessageId: String? = null,
    val sender: String? = null,
    val alarmScheduledTimeMillis: Long = 0L,
    val isAlarmEnabled: Boolean = true,
    val clockAppSent: Boolean = false,
    val hasConflict: Boolean = false,
    val conflictReason: String? = null,
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

data class DetectedMessage(
    val id: Long = 0,
    val source: MessageSource,
    val sender: String,
    val subject: String,
    val body: String,
    val receivedTimestamp: Long,
    val parsedShiftId: Long? = null,
    val matchedFilterReason: String,
    val isProcessed: Boolean = false,
    val extractedEmployee: String? = null,
    val extractedDate: String? = null,
    val extractedTimes: String? = null,
    val hasDiscrepancy: Boolean = false,
    val discrepancyDetails: String? = null
)

data class SenderFilter(
    val id: Long = 0,
    val pattern: String,
    val isKeyword: Boolean = false,
    val isSenderName: Boolean = false,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false
)

data class ParseDiscrepancy(
    val isSubjectBodyTimeConflict: Boolean = false,
    val isWeekdayMismatch: Boolean = false,
    val isAmbiguousDate: Boolean = false,
    val isPastShift: Boolean = false,
    val details: String = ""
)

data class ShiftParseResult(
    val isSuccessful: Boolean,
    val employeeName: String? = null,
    val shiftDateIso: String? = null, // YYYY-MM-DD
    val startTimeIso: String? = null, // HH:mm
    val endTimeIso: String? = null,   // HH:mm
    val displayTimeRange: String? = null,
    val department: String? = null,
    val discrepancies: List<ParseDiscrepancy> = emptyList(),
    val confidenceScore: Float = 0f,
    val rawSubject: String = "",
    val rawBody: String = ""
)
