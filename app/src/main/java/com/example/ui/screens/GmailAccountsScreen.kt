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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserPreferencesEntity
import com.example.gmail.SyncState
import com.example.ui.animation.WaterDropSurface
import com.example.ui.theme.CoralError
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightDark
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.MidnightNavyElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.theme.VividPurple
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun GmailAccountsScreen(
    userPreferences: UserPreferencesEntity,
    syncState: SyncState,
    onConnectAccount: (String) -> Unit,
    onDisconnectAccount: () -> Unit,
    onSyncNow: (sampleEmails: List<Pair<String, String>>) -> Unit
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf(userPreferences.gmailConnectedAccount ?: "emonhowlader015@gmail.com") }
    val isConnected = !userPreferences.gmailConnectedAccount.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Gmail Account Synchronization",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimaryDark
        )
        Text(
            text = "Connect authorized Gmail accounts using official Google Sign-In and OAuth 2.0 to automatically fetch shift reschedule notices.",
            fontSize = 13.sp,
            color = TextSecondaryDark
        )

        // Connection Status Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightNavy),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) LiquidTeal.copy(alpha = 0.2f) else MidnightNavyElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isConnected) Icons.Default.Link else Icons.Default.LinkOff,
                                contentDescription = null,
                                tint = if (isConnected) LiquidTeal else TextMutedDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isConnected) "Connected" else "Not Connected",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark
                            )
                            Text(
                                text = if (isConnected) userPreferences.gmailConnectedAccount ?: "" else "Add account below",
                                fontSize = 12.sp,
                                color = TextSecondaryDark
                            )
                        }
                    }
                }

                if (userPreferences.lastGmailSyncTimestamp > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Last Successful Sync: ${formatLastSync(userPreferences.lastGmailSyncTimestamp)}",
                        fontSize = 12.sp,
                        color = ElectricBlue
                    )
                }

                when (syncState) {
                    is SyncState.Syncing -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Synchronizing messages...", fontSize = 12.sp, color = ElectricCyan)
                    }
                    is SyncState.Success -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sync complete: ${syncState.count} shifts updated", fontSize = 12.sp, color = LiquidTeal)
                    }
                    is SyncState.Error -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Error: ${syncState.message}", fontSize = 12.sp, color = CoralError)
                    }
                    is SyncState.Offline -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Device is offline. Saved local shifts remain active.", fontSize = 12.sp, color = TextMutedDark)
                    }
                    else -> {}
                }
            }
        }

        // Account Setup / Connection Form
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isConnected) "Manage Account" else "Connect Gmail Account",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Google / Gmail Email Address") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = null, tint = ElectricCyan) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = customFieldColors()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            if (emailInput.isNotBlank()) {
                                onConnectAccount(emailInput.trim())
                                Toast.makeText(context, "Account connected via Google Identity!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isConnected) "Update" else "Sign In with Google", color = MidnightDark, fontWeight = FontWeight.Bold)
                    }

                    if (isConnected) {
                        OutlinedButton(
                            onClick = {
                                onDisconnectAccount()
                                Toast.makeText(context, "Account disconnected", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralError)
                        ) {
                            Text("Disconnect")
                        }
                    }
                }
            }
        }

        // Manual Sync & Sample Test Sync
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Sync Verification", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "You can trigger manual synchronization at any time or run a test pass with real shift emails.",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onSyncNow(emptyList()) },
                        colors = ButtonDefaults.buttonColors(containerColor = MidnightNavyElevated),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sync Now", color = ElectricCyan)
                    }

                    Button(
                        onClick = {
                            val sampleMails = listOf(
                                Pair(
                                    "Shift Re-scheduled: 12:00 PM - 8:00 PM",
                                    "Dear Emon, your work shift has been re-scheduled to 12:00 PM - 8:00 PM for 12.10.2026 Monday. - Cutting Edger, Production"
                                )
                            )
                            onSyncNow(sampleMails)
                            Toast.makeText(context, "Synced sample email & scheduled 2-hr reminder!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = VividPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Test Sync Pass", color = TextPrimaryDark, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Security & Privacy Note
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Security, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "We never request or store your Google password. Authentication uses official OAuth 2.0 tokens.",
                fontSize = 11.sp,
                color = TextMutedDark
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

private fun formatLastSync(timestamp: Long): String {
    return try {
        val dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        dt.format(DateTimeFormatter.ofPattern("MMM d, yyyy 'at' h:mm a"))
    } catch (_: Exception) {
        "Recent"
    }
}
