package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisGreenSuccess
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataVaultScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val contacts by viewModel.contactsRepository.contacts.collectAsState()
    val calls by viewModel.callsMessagesRepository.calls.collectAsState()
    val events by viewModel.calendarRepository.events.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showWipeAllDialog by remember { mutableStateOf(false) }
    var showWipeCallsDialog by remember { mutableStateOf(false) }
    var showWipeCalendarDialog by remember { mutableStateOf(false) }
    var showWipeContactsDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "STARK DATA VAULT & PRIVACY ERASER",
                        color = ArcCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Purge Contacts, Call Logs & Calendar Telemetry",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { showWipeAllDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("wipe_all_data_button")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Wipe All Data", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Voice instruction card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderCyan, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "VOICE DATA DELETION COMMANDS",
                            color = ArcCyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• \"Delete number [Name]\" / \"Remove contact [Name]\"\n• \"Delete call data\" / \"Wipe call logs\"\n• \"Delete calendar data\" / \"Clear schedule\"\n• \"कॉल डेटा हटाओ\" / \"नंबर डिलीट करो\"",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Vault Summary Counters
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Numbers
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (selectedTab == 0) ArcCyan else BorderCyan, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 0 },
                    colors = CardDefaults.cardColors(containerColor = if (selectedTab == 0) JarvisSurfaceElevated else JarvisSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Contacts, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${contacts.size}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Numbers", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                // Calls
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (selectedTab == 1) ArcCyan else BorderCyan, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 1 },
                    colors = CardDefaults.cardColors(containerColor = if (selectedTab == 1) JarvisSurfaceElevated else JarvisSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, tint = StarkGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${calls.size}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Call Logs", color = TextSecondary, fontSize = 10.sp)
                    }
                }

                // Calendar
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, if (selectedTab == 2) ArcCyan else BorderCyan, RoundedCornerShape(10.dp))
                        .clickable { selectedTab = 2 },
                    colors = CardDefaults.cardColors(containerColor = if (selectedTab == 2) JarvisSurfaceElevated else JarvisSurface),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = JarvisGreenSuccess, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${events.size}", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text("Cal Events", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // Numbers & Contacts
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ADDED NUMBERS & CONTACTS (${contacts.size})",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.contactsRepository.restoreDefaults() },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = ArcCyanDim, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", color = ArcCyanDim, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { showWipeContactsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Clear All", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                if (contacts.isEmpty()) {
                    item {
                        Text(
                            text = "All numbers and contact data have been erased from memory.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }

                items(contacts, key = { it.id }) { contact ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderCyan, RoundedCornerShape(10.dp))
                            .testTag("vault_contact_${contact.id}"),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(contact.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(contact.phoneNumber, color = ArcCyanDim, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                if (contact.affiliation.isNotBlank()) {
                                    Text(contact.affiliation, color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteContact(contact.id)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisRedAlert.copy(alpha = 0.15f))
                                    .testTag("delete_contact_button_${contact.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Number", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            1 -> {
                // Call Data & Logs
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CALL LOGS & TRANSCRIPT DATA (${calls.size})",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.callsMessagesRepository.restoreDefaults() },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = ArcCyanDim, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", color = ArcCyanDim, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { showWipeCallsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Wipe Call Logs", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                if (calls.isEmpty()) {
                    item {
                        Text(
                            text = "All call history, recordings, and screening transcripts have been erased.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }

                items(calls, key = { it.id }) { call ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderCyan, RoundedCornerShape(10.dp))
                            .testTag("vault_call_${call.id}"),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(call.contactName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${call.phoneNumber} • ${call.durationSeconds}s", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                if (call.jarvisAssessment.isNotBlank()) {
                                    Text(call.jarvisAssessment, color = ArcCyanDim, fontSize = 10.sp, maxLines = 1)
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteCallRecord(call.id)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisRedAlert.copy(alpha = 0.15f))
                                    .testTag("delete_call_button_${call.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Call", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            2 -> {
                // Calendar Data
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CALENDAR EVENTS & SCHEDULE (${events.size})",
                            color = ArcCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.calendarRepository.restoreDefaults() },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, tint = ArcCyanDim, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", color = ArcCyanDim, fontSize = 10.sp)
                            }

                            Button(
                                onClick = { showWipeCalendarDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Wipe Calendar", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
                }

                if (events.isEmpty()) {
                    item {
                        Text(
                            text = "All calendar schedule entries have been permanently purged.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                }

                items(events, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BorderCyan, RoundedCornerShape(10.dp))
                            .testTag("vault_cal_${event.id}"),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(event.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(event.startTimeMillis)),
                                    color = ArcCyanDim,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (event.location.isNotBlank()) {
                                    Text(event.location, color = TextSecondary, fontSize = 10.sp)
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteCalendarEvent(event.id)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JarvisRedAlert.copy(alpha = 0.15f))
                                    .testTag("delete_cal_button_${event.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Event", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Confirmation Dialogs
    if (showWipeAllDialog) {
        AlertDialog(
            onDismissRequest = { showWipeAllDialog = false },
            containerColor = JarvisSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = JarvisRedAlert)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EMERGENCY MAINFRAME WIPE", color = JarvisRedAlert, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "Are you sure you want to permanently erase all added phone numbers, call records, and calendar telemetry from this device?",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                        showWipeAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert)
                ) {
                    Text("Confirm Wipe", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeAllDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showWipeContactsDialog) {
        AlertDialog(
            onDismissRequest = { showWipeContactsDialog = false },
            containerColor = JarvisSurfaceElevated,
            title = { Text("Erase All Contacts", color = JarvisRedAlert, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Delete all contact phone numbers from Stark mainframe?", color = TextPrimary, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeContactsData()
                        showWipeContactsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert)
                ) {
                    Text("Erase Contacts", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeContactsDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showWipeCallsDialog) {
        AlertDialog(
            onDismissRequest = { showWipeCallsDialog = false },
            containerColor = JarvisSurfaceElevated,
            title = { Text("Wipe All Call Data", color = JarvisRedAlert, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Delete all incoming, outgoing, screened call logs, and interception transcripts?", color = TextPrimary, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeCallData()
                        showWipeCallsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert)
                ) {
                    Text("Purge Logs", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeCallsDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showWipeCalendarDialog) {
        AlertDialog(
            onDismissRequest = { showWipeCalendarDialog = false },
            containerColor = JarvisSurfaceElevated,
            title = { Text("Wipe Calendar Schedule", color = JarvisRedAlert, fontSize = 14.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Delete all scheduled meetings and briefings from calendar?", color = TextPrimary, fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeCalendarData()
                        showWipeCalendarDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert)
                ) {
                    Text("Purge Calendar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeCalendarDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
