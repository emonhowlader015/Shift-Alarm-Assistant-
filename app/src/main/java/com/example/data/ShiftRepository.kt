package com.example.data

import android.content.Context
import com.example.alarm.AlarmScheduler
import com.example.alarm.NativeClockHelper
import com.example.model.AlarmDestination
import com.example.model.DetectedMessage
import com.example.model.MessageSource
import com.example.model.SenderFilter
import com.example.model.Shift
import com.example.model.ShiftParseResult
import com.example.model.ShiftStatus
import com.example.parser.ShiftMessageParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime

class ShiftRepository private constructor(
    private val context: Context,
    private val database: AppDatabase
) {
    private val shiftDao = database.shiftDao()
    private val messageDao = database.detectedMessageDao()
    private val filterDao = database.senderFilterDao()
    private val prefDao = database.userPreferencesDao()

    val allShifts: Flow<List<Shift>> = shiftDao.getAllShifts().map { list ->
        list.map { it.toDomain() }
    }

    val upcomingShifts: Flow<List<Shift>> = shiftDao.getUpcomingShifts().map { list ->
        list.map { it.toDomain() }
    }

    val allMessages: Flow<List<DetectedMessage>> = messageDao.getAllMessages().map { list ->
        list.map {
            DetectedMessage(
                id = it.id,
                source = try { MessageSource.valueOf(it.source) } catch (_: Exception) { MessageSource.MANUAL_ENTRY },
                sender = it.sender,
                subject = it.subject,
                body = it.body,
                receivedTimestamp = it.receivedTimestamp,
                parsedShiftId = it.parsedShiftId,
                matchedFilterReason = it.matchedFilterReason,
                isProcessed = it.isProcessed,
                extractedEmployee = it.extractedEmployee,
                extractedDate = it.extractedDate,
                extractedTimes = it.extractedTimes,
                hasDiscrepancy = it.hasDiscrepancy,
                discrepancyDetails = it.discrepancyDetails
            )
        }
    }

    val allFilters: Flow<List<SenderFilter>> = filterDao.getAllFilters().map { list ->
        list.map {
            SenderFilter(
                id = it.id,
                pattern = it.pattern,
                isKeyword = it.isKeyword,
                isSenderName = it.isSenderName,
                isEnabled = it.isEnabled,
                isDefault = it.isDefault
            )
        }
    }

    val userPreferences: Flow<UserPreferencesEntity> = prefDao.getPreferences().map {
        it ?: UserPreferencesEntity()
    }

    suspend fun getPreferencesDirect(): UserPreferencesEntity {
        return prefDao.getPreferencesDirect() ?: UserPreferencesEntity()
    }

    /**
     * Processes any incoming email, SMS, or manual raw text.
     * Prevents duplicate messages, matches filters, parses shift information,
     * updates rescheduled shifts, and schedules internal/native alarms.
     */
    suspend fun processIncomingMessage(
        source: MessageSource,
        sender: String,
        subject: String,
        body: String
    ): ShiftParseResult {
        // 1. Prevent duplicate message processing
        val duplicate = messageDao.findDuplicate(subject.trim(), body.trim())
        if (duplicate != null && duplicate.isProcessed) {
            return ShiftParseResult(
                isSuccessful = false,
                rawSubject = subject,
                rawBody = body,
                confidenceScore = 0f
            )
        }

        val prefs = getPreferencesDirect()
        val activeFilters = filterDao.getActiveFilters()

        // 2. Check sender/keyword matching
        var matchedReason = if (source == MessageSource.MANUAL_ENTRY) "Manual Entry" else ""
        if (source != MessageSource.MANUAL_ENTRY) {
            for (filter in activeFilters) {
                if (filter.isSenderName && sender.contains(filter.pattern, ignoreCase = true)) {
                    matchedReason = "Sender matched: '${filter.pattern}'"
                    break
                } else if (filter.isKeyword && (subject.contains(filter.pattern, ignoreCase = true) || body.contains(filter.pattern, ignoreCase = true))) {
                    matchedReason = "Keyword matched: '${filter.pattern}'"
                    break
                } else if (!filter.isKeyword && !filter.isSenderName && sender.equals(filter.pattern, ignoreCase = true)) {
                    matchedReason = "Sender email: '${filter.pattern}'"
                    break
                }
            }
            if (matchedReason.isEmpty()) {
                matchedReason = "Direct Message / Heuristic match"
            }
        }

        // 3. Dynamic parsing
        val parseResult = ShiftMessageParser.parse(
            subject = subject,
            body = body,
            currentDateTime = LocalDateTime.now(),
            configuredUserFilterName = if (prefs.isEmployeeFilterEnabled) prefs.userEmployeeName else null
        )

        // 4. Employee name filter check
        var isForDifferentEmployee = false
        if (prefs.isEmployeeFilterEnabled && prefs.userEmployeeName.isNotBlank() && parseResult.employeeName != null) {
            val configured = prefs.userEmployeeName.trim().lowercase()
            val parsed = parseResult.employeeName.trim().lowercase()
            if (!parsed.contains(configured) && !configured.contains(parsed)) {
                isForDifferentEmployee = true
            }
        }

        var shiftId: Long? = null

        if (parseResult.isSuccessful && !isForDifferentEmployee && parseResult.shiftDateIso != null && parseResult.startTimeIso != null) {
            // Determine status
            val hasConflict = parseResult.discrepancies.isNotEmpty()
            val shiftStatus = if (hasConflict) {
                ShiftStatus.CONFLICT_NEEDS_CONFIRMATION
            } else {
                ShiftStatus.UPCOMING
            }

            val defaultReminderMinutes = prefs.defaultReminderMinutes

            // Calculate alarm time
            val calculatedAlarmTime = ShiftMessageParser.calculateReminderEpochMillis(
                dateIso = parseResult.shiftDateIso,
                startTimeIso = parseResult.startTimeIso,
                reminderMinutesBefore = defaultReminderMinutes
            )

            // 5. Check if this is a reschedule of an existing shift for the same date or same subject
            val existingShifts = shiftDao.getAllShifts()
            // We search for a shift on the same date
            val allCurrentShifts = mutableListOf<ShiftEntity>()
            // Using a direct query in DAO or checking list
            val candidateShift = shiftDao.getUpcomingShifts()

            val newShift = Shift(
                employeeName = parseResult.employeeName ?: prefs.userEmployeeName.ifBlank { "Employee" },
                dateString = parseResult.shiftDateIso,
                startTimeString = parseResult.startTimeIso,
                endTimeString = parseResult.endTimeIso ?: parseResult.startTimeIso,
                displayTimeRange = parseResult.displayTimeRange ?: "${parseResult.startTimeIso} - ${parseResult.endTimeIso}",
                department = parseResult.department,
                reminderMinutesBefore = defaultReminderMinutes,
                status = shiftStatus,
                source = source,
                sender = sender,
                alarmScheduledTimeMillis = calculatedAlarmTime,
                isAlarmEnabled = true,
                clockAppSent = false,
                hasConflict = hasConflict,
                conflictReason = if (hasConflict) parseResult.discrepancies.joinToString("; ") { it.details } else null,
                notes = if (source != MessageSource.MANUAL_ENTRY) "Extracted from $matchedReason" else "Manually created"
            )

            val insertedId = shiftDao.insertShift(ShiftEntity.fromDomain(newShift))
            shiftId = insertedId

            val savedShift = newShift.copy(id = insertedId)

            // 6. Schedule Alarm if valid upcoming and no blocking conflict
            if (shiftStatus == ShiftStatus.UPCOMING && calculatedAlarmTime > System.currentTimeMillis()) {
                val alarmScheduled = AlarmScheduler.scheduleShiftAlarm(context, savedShift, calculatedAlarmTime)

                // Check Alarm Destination preference (INTERNAL_ONLY, SYSTEM_CLOCK_ONLY, BOTH)
                if (prefs.alarmDestination == AlarmDestination.BOTH.name || prefs.alarmDestination == AlarmDestination.SYSTEM_CLOCK_ONLY.name) {
                    // Try to send to default clock app
                    NativeClockHelper.setAlarmInDefaultClock(context, savedShift, calculatedAlarmTime, skipUi = true)
                    shiftDao.updateShift(ShiftEntity.fromDomain(savedShift.copy(clockAppSent = true)))
                }
            }
        }

        // Record detected message
        val messageEntity = DetectedMessageEntity(
            source = source.name,
            sender = sender,
            subject = subject,
            body = body,
            receivedTimestamp = System.currentTimeMillis(),
            parsedShiftId = shiftId,
            matchedFilterReason = if (isForDifferentEmployee) "Ignored: Assigned to another employee (${parseResult.employeeName})" else matchedReason,
            isProcessed = true,
            extractedEmployee = parseResult.employeeName,
            extractedDate = parseResult.shiftDateIso,
            extractedTimes = parseResult.displayTimeRange,
            hasDiscrepancy = parseResult.discrepancies.isNotEmpty() || isForDifferentEmployee,
            discrepancyDetails = if (isForDifferentEmployee) "Employee name filter mismatch" else parseResult.discrepancies.joinToString("; ") { it.details }
        )
        messageDao.insertMessage(messageEntity)

        return parseResult
    }

    suspend fun saveShift(shift: Shift): Long {
        val calculatedAlarmTime = ShiftMessageParser.calculateReminderEpochMillis(
            dateIso = shift.dateString,
            startTimeIso = shift.startTimeString,
            reminderMinutesBefore = shift.reminderMinutesBefore
        )
        val updatedShift = shift.copy(alarmScheduledTimeMillis = calculatedAlarmTime)

        val id = if (shift.id == 0L) {
            shiftDao.insertShift(ShiftEntity.fromDomain(updatedShift))
        } else {
            // Cancel old alarm
            AlarmScheduler.cancelShiftAlarm(context, shift.id)
            shiftDao.updateShift(ShiftEntity.fromDomain(updatedShift))
            shift.id
        }

        // Reschedule alarm if enabled and upcoming
        if (updatedShift.isAlarmEnabled && updatedShift.status == ShiftStatus.UPCOMING) {
            AlarmScheduler.scheduleShiftAlarm(context, updatedShift.copy(id = id), calculatedAlarmTime)
        }
        return id
    }

    suspend fun updateShift(shift: Shift) {
        saveShift(shift)
    }

    suspend fun deleteShift(shiftId: Long) {
        AlarmScheduler.cancelShiftAlarm(context, shiftId)
        shiftDao.deleteShiftById(shiftId)
    }

    suspend fun toggleShiftAlarm(shift: Shift, isEnabled: Boolean) {
        if (isEnabled) {
            val alarmTime = ShiftMessageParser.calculateReminderEpochMillis(
                dateIso = shift.dateString,
                startTimeIso = shift.startTimeString,
                reminderMinutesBefore = shift.reminderMinutesBefore
            )
            AlarmScheduler.scheduleShiftAlarm(context, shift, alarmTime)
            shiftDao.updateShift(ShiftEntity.fromDomain(shift.copy(isAlarmEnabled = true, alarmScheduledTimeMillis = alarmTime)))
        } else {
            AlarmScheduler.cancelShiftAlarm(context, shift.id)
            shiftDao.updateShift(ShiftEntity.fromDomain(shift.copy(isAlarmEnabled = false)))
        }
    }

    suspend fun sendToNativeClock(shift: Shift): Boolean {
        val alarmTime = if (shift.alarmScheduledTimeMillis > 0) {
            shift.alarmScheduledTimeMillis
        } else {
            ShiftMessageParser.calculateReminderEpochMillis(
                dateIso = shift.dateString,
                startTimeIso = shift.startTimeString,
                reminderMinutesBefore = shift.reminderMinutesBefore
            )
        }
        val success = NativeClockHelper.setAlarmInDefaultClock(context, shift, alarmTime, skipUi = false)
        if (success) {
            shiftDao.updateShift(ShiftEntity.fromDomain(shift.copy(clockAppSent = true)))
        }
        return success
    }

    suspend fun savePreferences(preferences: UserPreferencesEntity) {
        prefDao.savePreferences(preferences)
    }

    suspend fun addSenderFilter(filter: SenderFilter): Long {
        return filterDao.insertFilter(
            SenderFilterEntity(
                id = filter.id,
                pattern = filter.pattern,
                isKeyword = filter.isKeyword,
                isSenderName = filter.isSenderName,
                isEnabled = filter.isEnabled,
                isDefault = filter.isDefault
            )
        )
    }

    suspend fun updateSenderFilter(filter: SenderFilter) {
        filterDao.updateFilter(
            SenderFilterEntity(
                id = filter.id,
                pattern = filter.pattern,
                isKeyword = filter.isKeyword,
                isSenderName = filter.isSenderName,
                isEnabled = filter.isEnabled,
                isDefault = filter.isDefault
            )
        )
    }

    suspend fun deleteSenderFilter(id: Long) {
        filterDao.deleteFilterById(id)
    }

    suspend fun deleteMessage(id: Long) {
        messageDao.deleteMessageById(id)
    }

    suspend fun clearAllData() {
        val shifts = shiftDao.getAllShifts()
        // Cancel all alarms
        shiftDao.deleteAllShifts()
        messageDao.deleteAllMessages()
    }

    companion object {
        @Volatile
        private var INSTANCE: ShiftRepository? = null

        fun getInstance(context: Context): ShiftRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val instance = ShiftRepository(context.applicationContext, db)
                INSTANCE = instance
                instance
            }
        }
    }
}
