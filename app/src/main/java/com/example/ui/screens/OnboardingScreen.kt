package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferencesEntity
import com.example.ui.animation.LiquidGlowBorder
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingMagenta
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple

@Composable
fun OnboardingScreen(
    initialPrefs: UserPreferencesEntity,
    onCompleteOnboarding: (UserPreferencesEntity) -> Unit,
    onSkip: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var employeeName by remember { mutableStateOf(initialPrefs.userEmployeeName) }
    var selectedMinutes by remember { mutableIntStateOf(initialPrefs.defaultReminderMinutes) }
    var destination by remember { mutableStateOf(initialPrefs.alarmDestination) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                1 -> {
                    // Step 1: Welcome
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(ElectricCyan, VividPurple, GlowingMagenta))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null, tint = MidnightDark, modifier = Modifier.size(44.dp))
                    }

                    Text(
                        text = "Shift Alarm Assistant",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark
                    )

                    Text(
                        text = "Developed by Emon Howlader",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ElectricCyan
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Never miss a rescheduled shift again. Automatically parses work schedule updates from Gmail and SMS, validates shift dates and times, and schedules reliable pre-shift alarms.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { step = 2 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Get Started", color = MidnightDark, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Skip Setup", color = TextSecondaryDark)
                    }
                }

                2 -> {
                    // Step 2: Employee Name
                    Text("Step 1 of 3: Employee Name", fontSize = 12.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    Text("Identify Your Messages", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text(
                        text = "Optionally set your name so the app knows which shifts belong to you when multiple coworker schedules appear in emails.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    OutlinedTextField(
                        value = employeeName,
                        onValueChange = { employeeName = it },
                        label = { Text("Your Name (e.g. Emon, Sarah, Rahim)") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = ElectricCyan) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customFieldColors()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { step = 3 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Continue", color = MidnightDark, fontWeight = FontWeight.Bold)
                    }
                }

                3 -> {
                    // Step 3: Default Reminder Interval
                    Text("Step 2 of 3: Reminder Interval", fontSize = 12.sp, color = ElectricCyan, fontWeight = FontWeight.Bold)
                    Text("When should the alarm ring?", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text("Default: 2 hours before your shift begins.", fontSize = 13.sp, color = TextSecondaryDark)

                    val presets = listOf(30, 60, 90, 120, 180, 240)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        presets.chunked(3).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { mins ->
                                    val isSel = selectedMinutes == mins
                                    WaterDropSurface(
                                        onClick = { selectedMinutes = mins },
                                        shape = RoundedCornerShape(10.dp),
                                        backgroundColor = if (isSel) VividPurple else MidnightCard,
                                        borderColor = if (isSel) ElectricCyan else MidnightNavyElevated,
                                        borderWidth = 1.dp,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (mins >= 60) "${mins / 60}h before" else "${mins}m before",
                                                fontSize = 12.sp,
                                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSel) TextPrimaryDark else TextSecondaryDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { step = 4 },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Continue", color = MidnightDark, fontWeight = FontWeight.Bold)
                    }
                }

                4 -> {
                    // Step 4: Finished
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LiquidTeal.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = LiquidTeal, modifier = Modifier.size(36.dp))
                    }

                    Text("All Set!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                    Text(
                        text = "Shift Alarm Assistant is configured and ready. You can test shift emails or SMS text anytime from the dashboard.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val updated = initialPrefs.copy(
                                userEmployeeName = employeeName.trim(),
                                isEmployeeFilterEnabled = employeeName.isNotBlank(),
                                defaultReminderMinutes = selectedMinutes,
                                hasCompletedOnboarding = true
                            )
                            onCompleteOnboarding(updated)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Enter Dashboard", color = MidnightDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
