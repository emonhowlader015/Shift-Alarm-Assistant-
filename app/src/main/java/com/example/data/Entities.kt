package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AlarmDestination
import com.example.model.MessageSource
import com.example.model.Shift
import com.example.model.ShiftStatus

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeName: String,
    val dateString: String,
    val startTimeString: String,
    val endTimeString: String,
    val displayTimeRange: String,
    val department: String? = null,
    val reminderMinutesBefore: Int = 120,
    val additionalRemindersJson: String = "",
    val status: String = ShiftStatus.UPCOMING.name,
    val source: String = MessageSource.MANUAL_ENTRY.name,
    val sourceMessageId: String? = null,
    val sender: String? = null,
    val alarmScheduledTimeMillis: Long = 0L,
    val isAlarmEnabled: Boolean = true,
    val clockAppSent: Boolean = false,
    val hasConflict: Boolean = false,
    val conflictReason: String? = null,
    val notes: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Shift {
        val extraReminders = if (additionalRemindersJson.isBlank()) emptyList()
        else additionalRemindersJson.split(",").mapNotNull { it.trim().toIntOrNull() }

        return Shift(
            id = id,
            employeeName = employeeName,
            dateString = dateString,
            startTimeString = startTimeString,
            endTimeString = endTimeString,
            displayTimeRange = displayTimeRange,
            department = department,
            reminderMinutesBefore = reminderMinutesBefore,
            additionalRemindersMinutes = extraReminders,
            status = try { ShiftStatus.valueOf(status) } catch (_: Exception) { ShiftStatus.UPCOMING },
            source = try { MessageSource.valueOf(source) } catch (_: Exception) { MessageSource.MANUAL_ENTRY },
            sourceMessageId = sourceMessageId,
            sender = sender,
            alarmScheduledTimeMillis = alarmScheduledTimeMillis,
            isAlarmEnabled = isAlarmEnabled,
            clockAppSent = clockAppSent,
            hasConflict = hasConflict,
            conflictReason = conflictReason,
            notes = notes,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(shift: Shift): ShiftEntity {
            return ShiftEntity(
                id = shift.id,
                employeeName = shift.employeeName,
                dateString = shift.dateString,
                startTimeString = shift.startTimeString,
                endTimeString = shift.endTimeString,
                displayTimeRange = shift.displayTimeRange,
                department = shift.department,
                reminderMinutesBefore = shift.reminderMinutesBefore,
                additionalRemindersJson = shift.additionalRemindersMinutes.joinToString(","),
                status = shift.status.name,
                source = shift.source.name,
                sourceMessageId = shift.sourceMessageId,
                sender = shift.sender,
                alarmScheduledTimeMillis = shift.alarmScheduledTimeMillis,
                isAlarmEnabled = shift.isAlarmEnabled,
                clockAppSent = shift.clockAppSent,
                hasConflict = shift.hasConflict,
                conflictReason = shift.conflictReason,
                notes = shift.notes,
                updatedAt = shift.updatedAt
            )
        }
    }
}

@Entity(tableName = "detected_messages")
data class DetectedMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String,
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

@Entity(tableName = "sender_filters")
data class SenderFilterEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,
    val isKeyword: Boolean = false,
    val isSenderName: Boolean = false,
    val isEnabled: Boolean = true,
    val isDefault: Boolean = false
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 1,
    val userEmployeeName: String = "",
    val isEmployeeFilterEnabled: Boolean = false,
    val defaultReminderMinutes: Int = 120,
    val alarmDestination: String = AlarmDestination.BOTH.name,
    val waterDropAnimationsEnabled: Boolean = true,
    val smsMonitoringEnabled: Boolean = false,
    val gmailConnectedAccount: String? = null,
    val lastGmailSyncTimestamp: Long = 0L,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)
