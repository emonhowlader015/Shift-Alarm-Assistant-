package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.MessageSource
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.AppBottomBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AlarmManagerScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DetectedMessagesScreen
import com.example.ui.screens.GmailAccountsScreen
import com.example.ui.screens.ManualImportScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PrivacyScreen
import com.example.ui.screens.ReminderStudioScreen
import com.example.ui.screens.SenderFiltersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShiftDetailScreen
import com.example.ui.screens.UpcomingShiftsScreen
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle intent opened from notification
        val shiftId = intent.getLongExtra("EXTRA_SHIFT_ID", -1L)

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    initialShiftId = shiftId
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    viewModel: MainViewModel,
    initialShiftId: Long
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val allShifts by viewModel.allShifts.collectAsStateWithLifecycle()
    val detectedMessages by viewModel.detectedMessages.collectAsStateWithLifecycle()
    val senderFilters by viewModel.senderFilters.collectAsStateWithLifecycle()
    val userPreferences by viewModel.userPreferences.collectAsStateWithLifecycle()
    val selectedShift by viewModel.selectedShift.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Notification permission launcher for Android 13+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            )
        }
        if (initialShiftId > 0) {
            val targetShift = allShifts.firstOrNull { it.id == initialShiftId }
            if (targetShift != null) {
                viewModel.selectShift(targetShift)
            }
        }
    }

    // Hardware / Gesture Back Handling
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD && currentScreen != AppScreen.ONBOARDING) {
        viewModel.navigateTo(AppScreen.DASHBOARD)
    }

    if (!userPreferences.hasCompletedOnboarding && currentScreen == AppScreen.ONBOARDING) {
        OnboardingScreen(
            initialPrefs = userPreferences,
            onCompleteOnboarding = { updated ->
                viewModel.updatePreferences(updated)
                viewModel.navigateTo(AppScreen.DASHBOARD)
            },
            onSkip = {
                viewModel.updatePreferences(userPreferences.copy(hasCompletedOnboarding = true))
                viewModel.navigateTo(AppScreen.DASHBOARD)
            }
        )
    } else {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(MidnightDark),
            containerColor = MidnightDark,
            topBar = {
                AppTopBar(
                    currentScreen = currentScreen,
                    onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) },
                    onNavigateTo = { viewModel.navigateTo(it) }
                )
            },
            bottomBar = {
                val hideBottomBar = currentScreen == AppScreen.SHIFT_DETAILS || currentScreen == AppScreen.MANUAL_IMPORT
                if (!hideBottomBar) {
                    AppBottomBar(
                        currentScreen = currentScreen,
                        onNavigateTo = { viewModel.navigateTo(it) }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MidnightDark)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            shifts = allShifts,
                            userPreferences = userPreferences,
                            syncState = syncState,
                            onNavigateTo = { viewModel.navigateTo(it) },
                            onSelectShift = { viewModel.selectShift(it) },
                            onToggleAlarm = { shift, enabled -> viewModel.toggleShiftAlarm(shift, enabled) },
                            onSyncNow = { viewModel.syncGmailNow() },
                            onTestAlarmNow = { viewModel.triggerTestAlarmNow(context) }
                        )
                    }
                    AppScreen.UPCOMING_SHIFTS -> {
                        UpcomingShiftsScreen(
                            shifts = allShifts,
                            onSelectShift = { viewModel.selectShift(it) },
                            onToggleAlarm = { shift, enabled -> viewModel.toggleShiftAlarm(shift, enabled) }
                        )
                    }
                    AppScreen.SHIFT_DETAILS -> {
                        ShiftDetailScreen(
                            shift = selectedShift,
                            onSaveShift = { viewModel.saveShift(it) },
                            onDeleteShift = { viewModel.deleteShift(it) },
                            onToggleAlarm = { shift, enabled -> viewModel.toggleShiftAlarm(shift, enabled) },
                            onSendToClock = { viewModel.sendShiftToNativeClock(it) },
                            onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                        )
                    }
                    AppScreen.DETECTED_MESSAGES -> {
                        DetectedMessagesScreen(
                            messages = detectedMessages,
                            onDeleteMessage = { viewModel.deleteDetectedMessage(it) }
                        )
                    }
                    AppScreen.MANUAL_IMPORT -> {
                        ManualImportScreen(
                            onProcessMessage = { sender, subject, body, source ->
                                viewModel.processMessageText(sender, subject, body, source)
                            },
                            onNavigateBack = { viewModel.navigateTo(AppScreen.DASHBOARD) }
                        )
                    }
                    AppScreen.REMINDER_STUDIO -> {
                        ReminderStudioScreen(
                            userPreferences = userPreferences,
                            upcomingShifts = allShifts,
                            onSaveDefaultReminder = { mins ->
                                viewModel.updatePreferences(userPreferences.copy(defaultReminderMinutes = mins))
                            }
                        )
                    }
                    AppScreen.GMAIL_ACCOUNTS -> {
                        GmailAccountsScreen(
                            userPreferences = userPreferences,
                            syncState = syncState,
                            onConnectAccount = { viewModel.connectGmail(it) },
                            onDisconnectAccount = { viewModel.disconnectGmail() },
                            onSyncNow = { sampleMails -> viewModel.syncGmailNow(sampleMails) }
                        )
                    }
                    AppScreen.SENDER_FILTERS -> {
                        SenderFiltersScreen(
                            filters = senderFilters,
                            userPreferences = userPreferences,
                            onAddFilter = { viewModel.addFilter(it) },
                            onUpdateFilter = { viewModel.updateFilter(it) },
                            onDeleteFilter = { viewModel.deleteFilter(it) },
                            onUpdatePreferences = { viewModel.updatePreferences(it) }
                        )
                    }
                    AppScreen.ALARM_MANAGER -> {
                        AlarmManagerScreen(
                            userPreferences = userPreferences,
                            upcomingShifts = allShifts,
                            onUpdatePreferences = { viewModel.updatePreferences(it) },
                            onTestInternalAlarm = { viewModel.triggerTestAlarmNow(context) }
                        )
                    }
                    AppScreen.SETTINGS -> {
                        SettingsScreen(
                            userPreferences = userPreferences,
                            onUpdatePreferences = { viewModel.updatePreferences(it) },
                            onNavigateTo = { viewModel.navigateTo(it) }
                        )
                    }
                    AppScreen.PRIVACY -> {
                        PrivacyScreen(
                            onClearAllData = { viewModel.clearAllData() }
                        )
                    }
                    AppScreen.ABOUT -> {
                        AboutScreen()
                    }
                    AppScreen.ONBOARDING -> {
                        OnboardingScreen(
                            initialPrefs = userPreferences,
                            onCompleteOnboarding = { updated ->
                                viewModel.updatePreferences(updated)
                                viewModel.navigateTo(AppScreen.DASHBOARD)
                            },
                            onSkip = {
                                viewModel.updatePreferences(userPreferences.copy(hasCompletedOnboarding = true))
                                viewModel.navigateTo(AppScreen.DASHBOARD)
                            }
                        )
                    }
                }
            }
        }
    }
}
