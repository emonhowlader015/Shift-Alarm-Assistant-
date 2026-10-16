package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alarm.NotificationHelper
import com.example.data.ShiftRepository
import com.example.data.UserPreferencesEntity
import com.example.gmail.GmailSyncManager
import com.example.gmail.SyncState
import com.example.model.AlarmDestination
import com.example.model.DetectedMessage
import com.example.model.MessageSource
import com.example.model.SenderFilter
import com.example.model.Shift
import com.example.model.ShiftParseResult
import com.example.model.ShiftStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class AppScreen {
    DASHBOARD,
    UPCOMING_SHIFTS,
    SHIFT_DETAILS,
    DETECTED_MESSAGES,
    MANUAL_IMPORT,
    REMINDER_STUDIO,
    GMAIL_ACCOUNTS,
    SENDER_FILTERS,
    ALARM_MANAGER,
    SETTINGS,
    PRIVACY,
    ABOUT,
    ONBOARDING
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ShiftRepository.getInstance(application)
    private val gmailSyncManager = GmailSyncManager(application, repository)

    val allShifts: StateFlow<List<Shift>> = repository.allShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val detectedMessages: StateFlow<List<DetectedMessage>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val senderFilters: StateFlow<List<SenderFilter>> = repository.allFilters
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPreferences: StateFlow<UserPreferencesEntity> = repository.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferencesEntity())

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedShift = MutableStateFlow<Shift?>(null)
    val selectedShift: StateFlow<Shift?> = _selectedShift.asStateFlow()

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val _lastParseResult = MutableStateFlow<ShiftParseResult?>(null)
    val lastParseResult: StateFlow<ShiftParseResult?> = _lastParseResult.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectShift(shift: Shift?) {
        _selectedShift.value = shift
        if (shift != null) {
            _currentScreen.value = AppScreen.SHIFT_DETAILS
        }
    }

    fun saveShift(shift: Shift) {
        viewModelScope.launch {
            val id = repository.saveShift(shift)
            _selectedShift.value = shift.copy(id = id)
        }
    }

    fun deleteShift(shiftId: Long) {
        viewModelScope.launch {
            repository.deleteShift(shiftId)
            if (_selectedShift.value?.id == shiftId) {
                _selectedShift.value = null
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }

    fun toggleShiftAlarm(shift: Shift, isEnabled: Boolean) {
        viewModelScope.launch {
            repository.toggleShiftAlarm(shift, isEnabled)
        }
    }

    fun sendShiftToNativeClock(shift: Shift) {
        viewModelScope.launch {
            repository.sendToNativeClock(shift)
        }
    }

    fun processMessageText(sender: String, subject: String, body: String, source: MessageSource = MessageSource.MANUAL_ENTRY) {
        viewModelScope.launch {
            val result = repository.processIncomingMessage(
                source = source,
                sender = sender.ifBlank { "Manual Input" },
                subject = subject,
                body = body
            )
            _lastParseResult.value = result
        }
    }

    fun syncGmailNow(customSampleMails: List<Pair<String, String>> = emptyList()) {
        viewModelScope.launch {
            _syncState.value = SyncState.Syncing
            val result = gmailSyncManager.performSync(customSampleMails)
            _syncState.value = result
        }
    }

    fun connectGmail(email: String) {
        viewModelScope.launch {
            gmailSyncManager.connectAccount(email)
        }
    }

    fun disconnectGmail() {
        viewModelScope.launch {
            gmailSyncManager.disconnectAccount()
        }
    }

    fun addFilter(filter: SenderFilter) {
        viewModelScope.launch {
            repository.addSenderFilter(filter)
        }
    }

    fun updateFilter(filter: SenderFilter) {
        viewModelScope.launch {
            repository.updateSenderFilter(filter)
        }
    }

    fun deleteFilter(id: Long) {
        viewModelScope.launch {
            repository.deleteSenderFilter(id)
        }
    }

    fun updatePreferences(prefs: UserPreferencesEntity) {
        viewModelScope.launch {
            repository.savePreferences(prefs)
        }
    }

    fun deleteDetectedMessage(id: Long) {
        viewModelScope.launch {
            repository.deleteMessage(id)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _selectedShift.value = null
        }
    }

    fun triggerTestAlarmNow(context: Context) {
        NotificationHelper.showShiftAlarmNotification(
            context = context,
            notificationId = 9999,
            shiftId = 0L,
            title = "⏰ Test Shift Alarm - Verified Active",
            message = "Shift Alarm Assistant is running! Your pre-shift reminders are configured and active."
        )
    }
}
