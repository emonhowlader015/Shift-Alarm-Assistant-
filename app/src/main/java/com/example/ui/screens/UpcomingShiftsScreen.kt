package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Shift
import com.example.model.ShiftStatus
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
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
import java.time.format.DateTimeFormatter

@Composable
fun UpcomingShiftsScreen(
    shifts: List<Shift>,
    onSelectShift: (Shift) -> Unit,
    onToggleAlarm: (Shift, Boolean) -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Needs Review", "All / Past")

    val displayedShifts = remember(shifts, selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> shifts.filter { it.status == ShiftStatus.UPCOMING }.sortedBy { it.dateString }
            1 -> shifts.filter { it.status == ShiftStatus.CONFLICT_NEEDS_CONFIRMATION || it.hasConflict }
            else -> shifts.sortedByDescending { it.dateString }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MidnightDark,
            contentColor = ElectricCyan,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ElectricCyan
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                val count = when (index) {
                    0 -> shifts.count { it.status == ShiftStatus.UPCOMING }
                    1 -> shifts.count { it.status == ShiftStatus.CONFLICT_NEEDS_CONFIRMATION || it.hasConflict }
                    else -> shifts.size
                }
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = "$title ($count)",
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTabIndex == index) ElectricCyan else TextMutedDark
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (displayedShifts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedTabIndex) {
                        0 -> "No upcoming shifts. Test or sync a new message to populate."
                        1 -> "No conflicts! All shift times and dates are verified."
                        else -> "No past shifts recorded."
                    },
                    color = TextMutedDark,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedShifts, key = { it.id }) { shift ->
                    ShiftListItemCard(
                        shift = shift,
                        onClick = { onSelectShift(shift) },
                        onToggleAlarm = { onToggleAlarm(shift, it) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ShiftListItemCard(
    shift: Shift,
    onClick: () -> Unit,
    onToggleAlarm: (Boolean) -> Unit
) {
    WaterDropSurface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MidnightNavy,
        borderColor = if (shift.hasConflict) AmberWarning.copy(alpha = 0.5f) else MidnightCardBorder,
        borderWidth = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shift.displayTimeRange,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${formatDateWithWeekday(shift.dateString)} • ${shift.employeeName}",
                        fontSize = 13.sp,
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

            if (!shift.department.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Role: ${shift.department}",
                    fontSize = 12.sp,
                    color = ElectricBlue
                )
            }

            if (shift.hasConflict) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberWarning.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = shift.conflictReason ?: "Timing or weekday conflict requires review",
                            fontSize = 11.sp,
                            color = AmberWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = VividPurple, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alarm: ${shift.reminderMinutesBefore}m before",
                        fontSize = 11.sp,
                        color = VividPurple,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (shift.clockAppSent) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clock app synced", fontSize = 11.sp, color = ElectricCyan)
                    }
                }
            }
        }
    }
}

private fun formatDateWithWeekday(dateIso: String): String {
    return try {
        val date = LocalDate.parse(dateIso)
        date.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))
    } catch (_: Exception) {
        dateIso
    }
}
