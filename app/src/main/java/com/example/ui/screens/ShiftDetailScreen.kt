package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Shift
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
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ShiftDetailScreen(
    shift: Shift?,
    onSaveShift: (Shift) -> Unit,
    onDeleteShift: (Long) -> Unit,
    onToggleAlarm: (Shift, Boolean) -> Unit,
    onSendToClock: (Shift) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    if (shift == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(MidnightDark),
            contentAlignment = Alignment.Center
        ) {
            Text("No shift selected", color = TextMutedDark)
        }
        return
    }

    var reminderMinutes by remember(shift) { mutableIntStateOf(shift.reminderMinutesBefore) }
    val presetOptions = listOf(15, 30, 45, 60, 90, 120, 180, 240)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Timing Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = shift.source.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Active Alarm", fontSize = 12.sp, color = TextSecondaryDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = shift.isAlarmEnabled,
                            onCheckedChange = { onToggleAlarm(shift, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightDark,
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextMutedDark,
                                uncheckedTrackColor = MidnightNavyElevated
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = shift.displayTimeRange,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatDetailDate(shift.dateString),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondaryDark
                    )
                }

                if (shift.hasConflict) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberWarning.copy(alpha = 0.2f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Discrepancy / Conflict Detected", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = shift.conflictReason ?: "The message subject and body disagree, or the stated weekday does not match the calendar date.",
                                fontSize = 12.sp,
                                color = AmberWarning
                            )
                        }
                    }
                }
            }
        }

        // Details Breakdown Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Shift Details", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)

                DetailRow(icon = Icons.Default.Person, label = "Employee Name", value = shift.employeeName, accent = ElectricCyan)
                DetailRow(icon = Icons.Default.Work, label = "Role / Department", value = shift.department ?: "Standard Shift", accent = VividPurple)
                DetailRow(icon = Icons.Default.Schedule, label = "Start - End Times", value = "${shift.startTimeString} - ${shift.endTimeString}", accent = LiquidTeal)
                if (!shift.sender.isNullOrBlank()) {
                    DetailRow(icon = Icons.Default.Schedule, label = "Detected Sender", value = shift.sender, accent = ElectricBlue)
                }
            }
        }

        // Reminder Timing Customizer
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminder Timing", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    Text(
                        text = formatReminderMinutes(reminderMinutes),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VividPurple
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Quick Presets:", fontSize = 12.sp, color = TextMutedDark)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetOptions.take(4).forEach { mins ->
                        PresetChip(
                            mins = mins,
                            isSelected = reminderMinutes == mins,
                            onClick = {
                                reminderMinutes = mins
                                onSaveShift(shift.copy(reminderMinutesBefore = mins))
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetOptions.drop(4).forEach { mins ->
                        PresetChip(
                            mins = mins,
                            isSelected = reminderMinutes == mins,
                            onClick = {
                                reminderMinutes = mins
                                onSaveShift(shift.copy(reminderMinutesBefore = mins))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                // Computed alarm time preview
                val previewTime = calculateAlarmPreview(shift.alarmScheduledTimeMillis)
                Text(
                    text = "Scheduled Alarm: $previewTime",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ElectricCyan
                )
            }
        }

        // Integration Actions
        WaterDropSurface(
            onClick = {
                onSendToClock(shift)
                Toast.makeText(context, "Opening Phone Default Clock...", Toast.LENGTH_SHORT).show()
            },
            shape = RoundedCornerShape(14.dp),
            backgroundColor = MidnightNavyElevated,
            borderColor = ElectricCyan.copy(alpha = 0.5f),
            borderWidth = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.AlarmOn, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Alarm in Phone's Default Clock App",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }

        // Delete Shift Button
        OutlinedButton(
            onClick = {
                onDeleteShift(shift.id)
                onNavigateBack()
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError)
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete Shift & Cancel Alarms")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 11.sp, color = TextMutedDark)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimaryDark)
        }
    }
}

@Composable
fun PresetChip(mins: Int, isSelected: Boolean, onClick: () -> Unit) {
    val text = if (mins >= 60) "${mins / 60}h" else "${mins}m"
    WaterDropSurface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = if (isSelected) VividPurple else MidnightNavyElevated,
        borderColor = if (isSelected) VividPurple else MidnightCardBorder,
        borderWidth = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else TextSecondaryDark
            )
        }
    }
}

private fun formatDetailDate(dateIso: String): String {
    return try {
        val date = LocalDate.parse(dateIso)
        date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
    } catch (_: Exception) {
        dateIso
    }
}

private fun formatReminderMinutes(mins: Int): String {
    return if (mins >= 60) {
        val h = mins / 60
        val m = mins % 60
        if (m == 0) "$h hours before" else "$h hrs $m mins before"
    } else {
        "$mins minutes before"
    }
}

private fun calculateAlarmPreview(alarmMillis: Long): String {
    if (alarmMillis <= 0) return "Pending calculation"
    return try {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(alarmMillis), ZoneId.systemDefault())
        dt.format(DateTimeFormatter.ofPattern("EEE, MMM d 'at' h:mm a"))
    } catch (_: Exception) {
        "Configured"
    }
}
