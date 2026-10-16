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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.animation.LiquidGlowBorder
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingMagenta
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Hero Logo & Title Box
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ElectricCyan, VividPurple, GlowingMagenta))),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Alarm, contentDescription = null, tint = MidnightDark, modifier = Modifier.size(40.dp))
        }

        Text(
            text = "Shift Alarm Assistant",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimaryDark
        )

        // Prominent Developer Credit per Section 1
        LiquidGlowBorder(shape = RoundedCornerShape(14.dp), borderWidth = 1.dp) {
            Box(
                modifier = Modifier
                    .background(MidnightNavy)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Developed by Emon Howlader",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan
                )
            }
        }

        Text(
            text = "Version 1.0.0 (Production Build)",
            fontSize = 12.sp,
            color = TextMutedDark
        )

        // Architectural Features Breakdown Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Core Architectural Capabilities", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)

                FeatureBullet("Dynamic Subject & Body Time Parsing", "Supports any 12h or 24h shift times without hardcoding fixed schedules or duration.")
                FeatureBullet("Dynamic Employee Name Detection", "Parses greetings across multiple name formats without hardcoding.")
                FeatureBullet("Date & Weekday Discrepancy Validation", "Cross-validates stated weekdays against calendar dates and device time.")
                FeatureBullet("Default 2-Hour Pre-Shift Calculation", "Alarm DateTime = Shift Start DateTime - 2 Hours in local timezone.")
                FeatureBullet("Reminder Timing Studio", "Fully customizable reminder intervals (5 mins to 24 hours, custom hours & minutes).")
                FeatureBullet("Dual Alarm Destination", "Maintains exact date AlarmManager schedules plus default Clock app prefill.")
                FeatureBullet("Offline-First Room Database", "Zero dependency on remote servers for shift alarms and local records.")
                FeatureBullet("Interactive RGB Water-Drop Touch Effects", "Smooth liquid ripples, neon glowing perimeters, and haptics.")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun FeatureBullet(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LiquidTeal, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
            Text(description, fontSize = 11.sp, color = TextSecondaryDark, lineHeight = 15.sp)
        }
    }
}
