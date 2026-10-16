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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.model.SenderFilter
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

@Composable
fun SenderFiltersScreen(
    filters: List<SenderFilter>,
    userPreferences: UserPreferencesEntity,
    onAddFilter: (SenderFilter) -> Unit,
    onUpdateFilter: (SenderFilter) -> Unit,
    onDeleteFilter: (Long) -> Unit,
    onUpdatePreferences: (UserPreferencesEntity) -> Unit
) {
    val context = LocalContext.current
    var newPattern by remember { mutableStateOf("") }
    var isKeywordType by remember { mutableStateOf(false) }

    var employeeFilterText by remember(userPreferences) {
        mutableStateOf(userPreferences.userEmployeeName)
    }
    var isEmployeeFilterActive by remember(userPreferences) {
        mutableStateOf(userPreferences.isEmployeeFilterEnabled)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sender & Keyword Filters",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "Customize authorized sender display names, sender email addresses, and subject keywords.",
                fontSize = 13.sp,
                color = TextSecondaryDark
            )
        }

        // Employee Name Filter Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Employee Name Filter", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                        }
                        Switch(
                            checked = isEmployeeFilterActive,
                            onCheckedChange = { active ->
                                isEmployeeFilterActive = active
                                onUpdatePreferences(
                                    userPreferences.copy(
                                        isEmployeeFilterEnabled = active,
                                        userEmployeeName = employeeFilterText.trim()
                                    )
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightDark,
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextMutedDark,
                                uncheckedTrackColor = MidnightNavyElevated
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "When enabled, messages addressed to other employees are ignored or flagged so their shifts are not accidentally assigned to you.",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = employeeFilterText,
                        onValueChange = {
                            employeeFilterText = it
                            onUpdatePreferences(
                                userPreferences.copy(
                                    userEmployeeName = it.trim(),
                                    isEmployeeFilterEnabled = isEmployeeFilterActive
                                )
                            )
                        },
                        label = { Text("Your Employee Name (e.g. Emon, Rahim, Sarah)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customFieldColors()
                    )
                }
            }
        }

        // Add New Filter Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Sender / Keyword Filter", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimaryDark)

                    OutlinedTextField(
                        value = newPattern,
                        onValueChange = { newPattern = it },
                        label = { Text(if (isKeywordType) "Subject Keyword" else "Sender Name or Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = customFieldColors()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isKeywordType = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isKeywordType) ElectricBlue else MidnightNavyElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Sender", color = if (!isKeywordType) MidnightDark else TextPrimaryDark)
                        }

                        Button(
                            onClick = { isKeywordType = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isKeywordType) VividPurple else MidnightNavyElevated
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Keyword", color = TextPrimaryDark)
                        }
                    }

                    Button(
                        onClick = {
                            if (newPattern.isNotBlank()) {
                                onAddFilter(
                                    SenderFilter(
                                        pattern = newPattern.trim(),
                                        isKeyword = isKeywordType,
                                        isSenderName = !isKeywordType,
                                        isEnabled = true
                                    )
                                )
                                newPattern = ""
                                Toast.makeText(context, "Filter added!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = MidnightDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Filter", color = MidnightDark, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Filters List
        item {
            Text(
                text = "Active Filter Rules (${filters.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimaryDark
            )
        }

        items(filters, key = { it.id }) { filter ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MidnightNavy),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (filter.isKeyword) Icons.Default.Tag else Icons.Default.FilterList,
                            contentDescription = null,
                            tint = if (filter.isKeyword) VividPurple else ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(filter.pattern, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text(
                                if (filter.isKeyword) "Subject Keyword" else "Sender Match",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = filter.isEnabled,
                            onCheckedChange = { onUpdateFilter(filter.copy(isEnabled = it)) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MidnightDark,
                                checkedTrackColor = ElectricCyan,
                                uncheckedThumbColor = TextMutedDark,
                                uncheckedTrackColor = MidnightNavyElevated
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(
                            onClick = { onDeleteFilter(filter.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMutedDark, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
