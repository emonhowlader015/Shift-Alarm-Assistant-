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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.data.UserPreferencesEntity
import com.example.model.Shift
import com.example.ui.animation.WaterDropSurface
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun ReminderStudioScreen(
    userPreferences: UserPreferencesEntity,
    upcomingShifts: List<Shift>,
    onSaveDefaultReminder: (Int) -> Unit
) {
    val context = LocalContext.current
    var selectedMinutes by remember(userPreferences) {
        mutableIntStateOf(userPreferences.defaultReminderMinutes)
    }

    var customHours by remember { mutableFloatStateOf(2f) }
    var customMinutes by remember { mutableFloatStateOf(0f) }
    var isCustomMode by remember { mutableStateOf(false) }

    // Secondary multiple reminders
    val multipleReminders = remember { mutableStateListOf(30, 240) }

    val presetDurations = listOf(
        Pair("5 min", 5),
        Pair("10 min", 10),
        Pair("15 min", 15),
        Pair("30 min", 30),
        Pair("45 min", 45),
        Pair("1 hour", 60),
        Pair("1.5 hours", 90),
        Pair("2 hours (Default)", 120),
        Pair("2.5 hours", 150),
        Pair("3 hours", 180),
        Pair("4 hours", 240),
        Pair("6 hours", 360),
        Pair("8 hours", 480),
        Pair("12 hours", 720),
        Pair("24 hours", 1440)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Reminder Timing Studio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )
        Text(
            text = "Fine-tune how long before your shift starts the alarm will ring. Supports single or multi-stage notifications.",
            fontSize = 13.sp,
            color = TextSecondaryDark
        )

        // Live Calculation Preview Card
        val sampleShiftDate = upcomingShifts.firstOrNull()?.dateString ?: LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val sampleShiftTime = upcomingShifts.firstOrNull()?.startTimeString ?: "12:00"

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LIVE CALCULATION PREVIEW", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ElectricCyan, letterSpacing = 1.sp)
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                val preview = calculateAlarmTimePreview(sampleShiftDate, sampleShiftTime, selectedMinutes)
                Text(
                    text = preview.first,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimaryDark
                )
                Text(
                    text = preview.second,
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Shift Start: $sampleShiftDate at $sampleShiftTime (Reminder: ${formatDuration(selectedMinutes)} prior)",
                    fontSize = 12.sp,
                    color = ElectricBlue
                )
            }
        }

        // Preset Duration Grid
        Text("Standard Presets", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val chunked = presetDurations.chunked(3)
            chunked.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowItems.forEach { (label, mins) ->
                        val isSelected = selectedMinutes == mins && !isCustomMode
                        WaterDropSurface(
                            onClick = {
                                selectedMinutes = mins
                                isCustomMode = false
                            },
                            shape = RoundedCornerShape(10.dp),
                            backgroundColor = if (isSelected) VividPurple else MidnightCard,
                            borderColor = if (isSelected) ElectricCyan else MidnightCardBorder,
                            borderWidth = 1.dp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextPrimaryDark,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Custom Duration Slider
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Custom Duration", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    Text(
                        text = "${customHours.toInt()}h ${customMinutes.toInt()}m",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Hours (0 - 168): ${customHours.toInt()}", fontSize = 12.sp, color = TextMutedDark)
                Slider(
                    value = customHours,
                    onValueChange = {
                        customHours = it
                        isCustomMode = true
                        selectedMinutes = (customHours.toInt() * 60) + customMinutes.toInt()
                    },
                    valueRange = 0f..24f,
                    steps = 23,
                    colors = SliderDefaults.colors(thumbColor = ElectricCyan, activeTrackColor = ElectricCyan)
                )

                Text("Minutes (0 - 59): ${customMinutes.toInt()}", fontSize = 12.sp, color = TextMutedDark)
                Slider(
                    value = customMinutes,
                    onValueChange = {
                        customMinutes = it
                        isCustomMode = true
                        selectedMinutes = (customHours.toInt() * 60) + customMinutes.toInt()
                    },
                    valueRange = 0f..55f,
                    steps = 10,
                    colors = SliderDefaults.colors(thumbColor = VividPurple, activeTrackColor = VividPurple)
                )
            }
        }

        // Multiple Reminders Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Multiple Reminders Per Shift", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                    IconButton(
                        onClick = {
                            multipleReminders.add(60)
                            Toast.makeText(context, "Added 1-hour follow-up alert", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = ElectricCyan)
                    }
                }
                Text("Add multi-tier reminders (e.g. 4h prep alert, 2h primary alarm, 30m departure call).", fontSize = 12.sp, color = TextSecondaryDark)

                Spacer(modifier = Modifier.height(10.dp))

                multipleReminders.forEachIndexed { index, mins ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = LiquidTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secondary Alert: ${formatDuration(mins)} prior", fontSize = 13.sp, color = TextPrimaryDark)
                        }
                        IconButton(
                            onClick = { multipleReminders.removeAt(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Save as Global Default Button
        Button(
            onClick = {
                onSaveDefaultReminder(selectedMinutes)
                Toast.makeText(context, "Saved ${formatDuration(selectedMinutes)} as default pre-shift reminder!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = MidnightDark, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save As Global Default (${formatDuration(selectedMinutes)})", color = MidnightDark, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun formatDuration(mins: Int): String {
    return if (mins >= 60) {
        val h = mins / 60
        val m = mins % 60
        if (m == 0) "$h hr" else "$h hr $m min"
    } else {
        "$mins min"
    }
}

private fun calculateAlarmTimePreview(dateIso: String, timeIso: String, reminderMins: Int): Pair<String, String> {
    return try {
        val parts = timeIso.split(":")
        val hour = parts[0].toInt()
        val min = parts[1].toInt()
        val shiftStart = LocalDateTime.of(LocalDate.parse(dateIso), LocalTime.of(hour, min))
        val alarmDateTime = shiftStart.minusMinutes(reminderMins.toLong())

        val timeStr = alarmDateTime.format(DateTimeFormatter.ofPattern("h:mm a"))
        val dateStr = alarmDateTime.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))
        Pair(timeStr, dateStr)
    } catch (_: Exception) {
        Pair("Calculation pending", "Select valid shift")
    }
}
