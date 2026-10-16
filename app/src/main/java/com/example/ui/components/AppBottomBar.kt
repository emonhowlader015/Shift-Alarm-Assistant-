package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppScreen
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.VividPurple

@Composable
fun AppBottomBar(
    currentScreen: AppScreen,
    onNavigateTo: (AppScreen) -> Unit
) {
    val items = listOf(
        NavigationItem("Home", Icons.Default.Dashboard, AppScreen.DASHBOARD),
        NavigationItem("Shifts", Icons.Default.CalendarMonth, AppScreen.UPCOMING_SHIFTS),
        NavigationItem("Messages", Icons.Default.Email, AppScreen.DETECTED_MESSAGES),
        NavigationItem("Studio", Icons.Default.AccessTime, AppScreen.REMINDER_STUDIO),
        NavigationItem("Alarms", Icons.Default.Alarm, AppScreen.ALARM_MANAGER)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MidnightDark)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentScreen == item.screen
                WaterDropSurface(
                    onClick = { onNavigateTo(item.screen) },
                    shape = CircleShape,
                    backgroundColor = if (isSelected) MidnightNavy else Color.Transparent,
                    borderColor = if (isSelected) ElectricCyan.copy(alpha = 0.5f) else Color.Transparent,
                    borderWidth = 1.dp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) ElectricCyan else TextMutedDark,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimaryDark else TextMutedDark
                        )
                    }
                }
            }
        }
    }
}

private data class NavigationItem(
    val label: String,
    val icon: ImageVector,
    val screen: AppScreen
)
