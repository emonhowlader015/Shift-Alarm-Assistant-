package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingMagenta
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.VividPurple

@Composable
fun AppTopBar(
    currentScreen: AppScreen,
    onNavigateBack: () -> Unit,
    onNavigateTo: (AppScreen) -> Unit
) {
    val isHome = currentScreen == AppScreen.DASHBOARD

    val screenTitle = when (currentScreen) {
        AppScreen.DASHBOARD -> "Shift Alarm Assistant"
        AppScreen.UPCOMING_SHIFTS -> "Upcoming Shifts"
        AppScreen.SHIFT_DETAILS -> "Shift Details"
        AppScreen.DETECTED_MESSAGES -> "Detected Messages"
        AppScreen.MANUAL_IMPORT -> "Test & Import Message"
        AppScreen.REMINDER_STUDIO -> "Reminder Timing Studio"
        AppScreen.GMAIL_ACCOUNTS -> "Gmail Accounts"
        AppScreen.SENDER_FILTERS -> "Sender Filters"
        AppScreen.ALARM_MANAGER -> "Alarm Manager"
        AppScreen.SETTINGS -> "Settings"
        AppScreen.PRIVACY -> "Privacy & Security"
        AppScreen.ABOUT -> "About"
        AppScreen.ONBOARDING -> "Setup Wizard"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MidnightDark, MidnightNavy)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isHome && currentScreen != AppScreen.ONBOARDING) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ElectricCyan
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(ElectricCyan, VividPurple, GlowingMagenta))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = "Logo",
                        tint = MidnightDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Text(
                text = screenTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                modifier = Modifier.weight(1f)
            )

            if (isHome) {
                IconButton(
                    onClick = { onNavigateTo(AppScreen.SETTINGS) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = ElectricBlue
                    )
                }
                IconButton(
                    onClick = { onNavigateTo(AppScreen.ABOUT) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About",
                        tint = ElectricCyan
                    )
                }
            }
        }
    }
}
