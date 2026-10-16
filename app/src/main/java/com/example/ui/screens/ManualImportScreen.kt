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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.model.MessageSource
import com.example.model.ShiftParseResult
import com.example.parser.ShiftMessageParser
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
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
import java.time.LocalDateTime

@Composable
fun ManualImportScreen(
    onProcessMessage: (sender: String, subject: String, body: String, source: MessageSource) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var senderInput by remember { mutableStateOf("CuttingEdger-PayRol <payroll@cuttingedger.com>") }
    var subjectInput by remember { mutableStateOf("Shift Re-scheduled: 12:00 PM - 8:00 PM") }
    var bodyInput by remember {
        mutableStateOf("Dear Emon, your work shift has been re-scheduled to 12:00 PM - 8:00 PM for 12.10.2026 Monday. - Cutting Edger, Production")
    }

    var liveParseResult by remember {
        mutableStateOf<ShiftParseResult?>(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Dynamic Message Parser Sandbox",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )
        Text(
            text = "Test or import any shift notification (Gmail body, SMS text, or custom message). Start & end times, employee names, and dates are dynamically parsed and validated in real time.",
            fontSize = 13.sp,
            color = TextSecondaryDark
        )

        // Preset Test Case Chips
        Text("Quick Test Samples:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = ElectricCyan)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SampleChip(
                label = "Emon (12-8 PM)",
                onClick = {
                    senderInput = "CuttingEdger-PayRol <payroll@cuttingedger.com>"
                    subjectInput = "Shift Re-scheduled: 12:00 PM - 8:00 PM"
                    bodyInput = "Dear Emon, your work shift has been re-scheduled to 12:00 PM - 8:00 PM for 12.10.2026 Monday. - Cutting Edger, Production"
                }
            )
            SampleChip(
                label = "Rahim (7-3 AM)",
                onClick = {
                    senderInput = "WorkScheduler <schedule@company.org>"
                    subjectInput = "Shift Re-scheduled: 7:00 AM - 3:00 PM"
                    bodyInput = "Dear Rahim, your work shift has been re-scheduled to 7:00 AM - 3:00 PM for 15.10.2026 Thursday. - Logistics, Packaging"
                }
            )
            SampleChip(
                label = "Sarah (Overnight)",
                onClick = {
                    senderInput = "ShiftAlert SMS"
                    subjectInput = "Shift Re-scheduled"
                    bodyInput = "Dear Sarah, your work shift has been re-scheduled to 10:00 PM - 6:00 AM for 18.10.2026 Sunday. - Quality Assurance"
                }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SampleChip(
                label = "24-Hour Format (Alex)",
                onClick = {
                    senderInput = "HR Operations"
                    subjectInput = "Shift Re-scheduled: 14:00 - 22:00"
                    bodyInput = "Hi Alex, your work shift has been re-scheduled to 14:00 - 22:00 for 2026-10-22. - Maintenance Team"
                }
            )
            SampleChip(
                label = "Conflict Test (Jordan)",
                onClick = {
                    senderInput = "Notifications"
                    subjectInput = "Shift Re-scheduled: 9:00 AM - 5:00 PM"
                    bodyInput = "Dear Jordan, your work shift has been re-scheduled to 12:00 PM - 8:00 PM for 12.10.2026 Wednesday. - Dispatch"
                }
            )
        }

        // Input Fields
        OutlinedTextField(
            value = senderInput,
            onValueChange = { senderInput = it },
            label = { Text("Sender Email / Name") },
            modifier = Modifier.fillMaxWidth(),
            colors = customFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = subjectInput,
            onValueChange = { subjectInput = it },
            label = { Text("Email Subject or SMS Header") },
            modifier = Modifier.fillMaxWidth(),
            colors = customFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = bodyInput,
            onValueChange = { bodyInput = it },
            label = { Text("Message Body Text") },
            minLines = 3,
            maxLines = 6,
            modifier = Modifier.fillMaxWidth(),
            colors = customFieldColors(),
            shape = RoundedCornerShape(12.dp)
        )

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    liveParseResult = ShiftMessageParser.parse(
                        subject = subjectInput,
                        body = bodyInput,
                        currentDateTime = LocalDateTime.now()
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MidnightNavyElevated),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Analyze Text", color = ElectricCyan)
            }

            Button(
                onClick = {
                    val result = ShiftMessageParser.parse(
                        subject = subjectInput,
                        body = bodyInput,
                        currentDateTime = LocalDateTime.now()
                    )
                    liveParseResult = result
                    onProcessMessage(senderInput, subjectInput, bodyInput, MessageSource.MANUAL_ENTRY)
                    Toast.makeText(context, "Shift imported and alarm scheduled!", Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                },
                modifier = Modifier.weight(1.3f),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save & Set Alarm", color = MidnightDark, fontWeight = FontWeight.Bold)
            }
        }

        // Live Parsing Breakdown Card
        liveParseResult?.let { result ->
            ParseResultCard(result)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun SampleChip(label: String, onClick: () -> Unit) {
    WaterDropSurface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MidnightCard,
        borderColor = MidnightCardBorder,
        borderWidth = 1.dp
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontSize = 11.sp, color = TextPrimaryDark, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ParseResultCard(result: ShiftParseResult) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightNavy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dynamic Parse Results", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (result.isSuccessful) LiquidTeal.copy(alpha = 0.2f) else CoralError.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (result.isSuccessful) "VALID SHIFT (${(result.confidenceScore * 100).toInt()}%)" else "INCOMPLETE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isSuccessful) LiquidTeal else CoralError
                    )
                }
            }

            DetailRow(icon = Icons.Default.Person, label = "Employee Name (Dynamic)", value = result.employeeName ?: "Unknown", accent = ElectricCyan)
            DetailRow(icon = Icons.Default.DateRange, label = "Shift Date", value = result.shiftDateIso ?: "Could not detect date", accent = VividPurple)
            DetailRow(icon = Icons.Default.Schedule, label = "Shift Times", value = result.displayTimeRange ?: "Could not detect times", accent = LiquidTeal)

            if (!result.department.isNullOrBlank()) {
                DetailRow(icon = Icons.Default.Schedule, label = "Department / Role", value = result.department, accent = ElectricBlue)
            }

            if (result.discrepancies.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AmberWarning.copy(alpha = 0.15f))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Discrepancies Flagged:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                        result.discrepancies.forEach { disc ->
                            Text("• ${disc.details}", fontSize = 11.sp, color = AmberWarning, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun customFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ElectricCyan,
    unfocusedBorderColor = MidnightCardBorder,
    focusedLabelColor = ElectricCyan,
    unfocusedLabelColor = TextMutedDark,
    focusedTextColor = TextPrimaryDark,
    unfocusedTextColor = TextPrimaryDark,
    focusedContainerColor = MidnightCard,
    unfocusedContainerColor = MidnightCard
)
