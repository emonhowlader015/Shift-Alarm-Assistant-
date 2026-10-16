package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferencesEntity
import com.example.gmail.SyncState
import com.example.model.AlarmDestination
import com.example.model.Shift
import com.example.model.ShiftStatus
import com.example.ui.AppScreen
import com.example.ui.animation.LiquidGlowBorder
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingMagenta
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightCardBorder
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DashboardScreen(
    shifts: List<Shift>,
    userPreferences: UserPreferencesEntity,
    syncState: SyncState,
    onNavigateTo: (AppScreen) -> Unit,
    onSelectShift: (Shift) -> Unit,
    onToggleAlarm: (Shift, Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onTestAlarmNow: () -> Unit
) {
    val context = LocalContext.current
    val upcomingShifts = remember(shifts) {
        shifts.filter { it.status == ShiftStatus.UPCOMING || it.status == ShiftStatus.CONFLICT_NEEDS_CONFIRMATION }
            .sortedBy { it.dateString + " " + it.startTimeString }
    }
    val nextShift = upcomingShifts.firstOrNull()

    // Countdown timer tick
    var currentMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentMillis = System.currentTimeMillis()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Welcome Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (userPreferences.userEmployeeName.isNotBlank()) "Hello, ${userPreferences.userEmployeeName}" else "Welcome",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Text(
                        text = "Automated shift alarms & synchronization",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )
                }

                WaterDropSurface(
                    onClick = onSyncNow,
                    shape = RoundedCornerShape(12.dp),
                    backgroundColor = MidnightNavyElevated,
                    borderColor = ElectricCyan.copy(alpha = 0.4f),
                    borderWidth = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            tint = ElectricCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync", fontSize = 12.sp, color = ElectricCyan, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // 1. Next Upcoming Shift Hero Card
        item {
            if (nextShift != null) {
                NextShiftHeroCard(
                    shift = nextShift,
                    currentMillis = currentMillis,
                    onSelect = { onSelectShift(nextShift) },
                    onToggleAlarm = { onToggleAlarm(nextShift, it) }
                )
            } else {
                EmptyStateShiftCard(onImportClick = { onNavigateTo(AppScreen.MANUAL_IMPORT) })
            }
        }

        // 2. Integration & Telemetry Status Card
        item {
            IntegrationStatusCard(
                userPreferences = userPreferences,
                syncState = syncState,
                onNavigateTo = onNavigateTo
            )
        }

        // 3. Quick Action Hub
        item {
            Text(
                text = "Quick Actions",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Manual Parser Sandbox
                QuickActionButton(
                    title = "Test Parser",
                    subtitle = "Paste raw email/SMS",
                    icon = Icons.Default.Add,
                    glowColor = ElectricCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.MANUAL_IMPORT) }
                )

                // Reminder Studio
                QuickActionButton(
                    title = "Timing Studio",
                    subtitle = "Customize alarms",
                    icon = Icons.Default.Tune,
                    glowColor = VividPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateTo(AppScreen.REMINDER_STUDIO) }
                )

                // Test Alarm Trigger
                QuickActionButton(
                    title = "Test Alarm",
                    subtitle = "Ring & notify",
                    icon = Icons.Default.NotificationsActive,
                    glowColor = GlowingMagenta,
                    modifier = Modifier.weight(1f),
                    onClick = onTestAlarmNow
                )
            }
        }

        // 4. Upcoming Shift List Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scheduled Shifts (${upcomingShifts.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )

                if (upcomingShifts.isNotEmpty()) {
                    Text(
                        text = "View all",
                        fontSize = 13.sp,
                        color = ElectricCyan,
                        modifier = Modifier.clickable { onNavigateTo(AppScreen.UPCOMING_SHIFTS) }
                    )
                }
            }
        }

        if (upcomingShifts.isEmpty()) {
            item {
                Text(
                    text = "No upcoming shifts detected yet. Connect Gmail or paste an SMS notification above.",
                    fontSize = 13.sp,
                    color = TextMutedDark,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(upcomingShifts.take(3)) { shift ->
                CompactShiftCard(
                    shift = shift,
                    onClick = { onSelectShift(shift) },
                    onToggleAlarm = { onToggleAlarm(shift, it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NextShiftHeroCard(
    shift: Shift,
    currentMillis: Long,
    onSelect: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit
) {
    LiquidGlowBorder(
        shape = RoundedCornerShape(20.dp),
        borderWidth = 1.5.dp
    ) {
        WaterDropSurface(
            onClick = onSelect,
            shape = RoundedCornerShape(20.dp),
            backgroundColor = MidnightNavy,
            borderColor = Color.Transparent,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NEXT UPCOMING SHIFT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan,
                            letterSpacing = 1.sp
                        )
                    }

                    Switch(
                        checked = shift.isAlarmEnabled,
                        onCheckedChange = onToggleAlarm,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MidnightDark,
                            checkedTrackColor = ElectricCyan,
                            uncheckedThumbColor = TextMutedDark,
                            uncheckedTrackColor = MidnightNavyElevated
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Shift Time & Date
                Text(
                    text = shift.displayTimeRange,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryDark
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatFriendlyDate(shift.dateString),
                        fontSize = 15.sp,
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium
                    )
                    if (!shift.department.isNullOrBlank()) {
                        Text(
                            text = " • ${shift.department}",
                            fontSize = 14.sp,
                            color = ElectricCyan
                        )
                    }
                }

                // Discrepancy warning banner if conflict
                if (shift.hasConflict) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberWarning.copy(alpha = 0.2f))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = shift.conflictReason ?: "Discrepancy detected between subject & body",
                                fontSize = 12.sp,
                                color = AmberWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Countdown and Reminder Time Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Countdown box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightNavyElevated)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Starts in", fontSize = 11.sp, color = TextMutedDark)
                            Text(
                                text = calculateRemainingTime(shift.dateString, shift.startTimeString, currentMillis),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = LiquidTeal
                            )
                        }
                    }

                    // Scheduled Reminder Time box
                    Box(
                        modifier = Modifier
                            .weight(1.2f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MidnightNavyElevated)
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Reminder Alarm", fontSize = 11.sp, color = TextMutedDark)
                            Text(
                                text = formatReminderTime(shift.alarmScheduledTimeMillis, shift.reminderMinutesBefore),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = VividPurple
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateShiftCard(onImportClick: () -> Unit) {
    WaterDropSurface(
        onClick = onImportClick,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MidnightNavy,
        borderColor = ElectricCyan.copy(alpha = 0.3f),
        borderWidth = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No Shifts Scheduled",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Connect your Gmail or paste any shift email/SMS to auto-detect timing and set alarms.",
                fontSize = 13.sp,
                color = TextSecondaryDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onImportClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Test & Import Shift Message", color = MidnightDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun IntegrationStatusCard(
    userPreferences: UserPreferencesEntity,
    syncState: SyncState,
    onNavigateTo: (AppScreen) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sync & Alarm Status",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Gmail Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(AppScreen.GMAIL_ACCOUNTS) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Gmail Synchronization", fontSize = 13.sp, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
                    val gmailStatus = if (!userPreferences.gmailConnectedAccount.isNullOrBlank()) {
                        "Connected: ${userPreferences.gmailConnectedAccount}"
                    } else {
                        "Not connected (tap to configure)"
                    }
                    Text(gmailStatus, fontSize = 11.sp, color = TextSecondaryDark)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // SMS Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(AppScreen.SETTINGS) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Sms, contentDescription = null, tint = VividPurple, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("SMS Detection", fontSize = 13.sp, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
                    Text(
                        if (userPreferences.smsMonitoringEnabled) "Monitoring incoming shift SMS" else "Disabled (Tap to enable)",
                        fontSize = 11.sp,
                        color = TextSecondaryDark
                    )
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Destination Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateTo(AppScreen.ALARM_MANAGER) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Alarm, contentDescription = null, tint = LiquidTeal, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Alarm Target", fontSize = 13.sp, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
                    val targetText = when (userPreferences.alarmDestination) {
                        "INTERNAL_ONLY" -> "Shift Alarm Assistant Internal Scheduler"
                        "SYSTEM_CLOCK_ONLY" -> "Device Default Clock App"
                        else -> "Dual: Internal Scheduler + Device Clock"
                    }
                    Text(targetText, fontSize = 11.sp, color = TextSecondaryDark)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = TextMutedDark, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    glowColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    WaterDropSurface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        backgroundColor = MidnightCard,
        borderColor = glowColor.copy(alpha = 0.35f),
        borderWidth = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(glowColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = glowColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(subtitle, fontSize = 10.sp, color = TextMutedDark, maxLines = 1)
        }
    }
}

@Composable
fun CompactShiftCard(
    shift: Shift,
    onClick: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit
) {
    WaterDropSurface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        backgroundColor = MidnightCard,
        borderColor = MidnightCardBorder,
        borderWidth = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = shift.displayTimeRange,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    if (shift.hasConflict) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.Warning, contentDescription = "Conflict", tint = AmberWarning, modifier = Modifier.size(14.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${shift.employeeName} • ${formatFriendlyDate(shift.dateString)}",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
            }

            Switch(
                checked = shift.isAlarmEnabled,
                onCheckedChange = onToggleAlarm,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MidnightDark,
                    checkedTrackColor = ElectricCyan,
                    uncheckedThumbColor = TextMutedDark,
                    uncheckedTrackColor = MidnightNavyElevated
                )
            )
        }
    }
}

private fun formatFriendlyDate(dateIso: String): String {
    return try {
        val date = LocalDate.parse(dateIso)
        date.format(DateTimeFormatter.ofPattern("EEE, MMM d, yyyy"))
    } catch (_: Exception) {
        dateIso
    }
}

private fun calculateRemainingTime(dateIso: String, timeIso: String, currentMillis: Long): String {
    return try {
        val parts = timeIso.split(":")
        val hour = parts[0].toInt()
        val min = parts[1].toInt()
        val shiftStart = LocalDateTime.of(LocalDate.parse(dateIso), LocalTime.of(hour, min))
        val targetMillis = shiftStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val diff = targetMillis - currentMillis
        if (diff <= 0) return "Shift now / past"

        val hours = diff / (1000 * 60 * 60)
        val mins = (diff / (1000 * 60)) % 60
        if (hours > 24) {
            val days = hours / 24
            "${days}d ${hours % 24}h"
        } else {
            "${hours}h ${mins}m"
        }
    } catch (_: Exception) {
        "Upcoming"
    }
}

private fun formatReminderTime(alarmMillis: Long, minutesBefore: Int): String {
    if (alarmMillis <= 0) return "${minutesBefore}m before"
    return try {
        val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarmMillis), ZoneId.systemDefault())
        val timeFormatted = dateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
        val durationFormatted = if (minutesBefore >= 60) {
            val h = minutesBefore / 60
            val m = minutesBefore % 60
            if (m == 0) "${h}h before" else "${h}h ${m}m before"
        } else {
            "${minutesBefore}m before"
        }
        "$timeFormatted ($durationFormatted)"
    } catch (_: Exception) {
        "${minutesBefore}m before"
    }
}
