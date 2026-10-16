package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.CalendarEventRecord
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: JarvisViewModel,
    onRequestCalendarPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val events by viewModel.calendarRepository.events.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var eventTitle by remember { mutableStateOf("") }
    var eventDesc by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var eventHoursAhead by remember { mutableStateOf("2") }

    val nextEvent = events.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        LazyColumn(
            modifier = Modifier
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
                            text = "STARK EXECUTIVE SCHEDULE",
                            color = ArcCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Calendar Sync & Voice Event Scheduling",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                onRequestCalendarPermission()
                                scope.launch {
                                    viewModel.calendarRepository.refreshCalendar()
                                }
                            },
                            modifier = Modifier.testTag("refresh_calendar_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Sync", tint = ArcCyanGlow)
                        }

                        Button(
                            onClick = {
                                if (events.isNotEmpty()) {
                                    val top = events.take(2).joinToString(". ") {
                                        "${it.title} at ${SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(it.startTimeMillis))}"
                                    }
                                    viewModel.voiceManager.speak("Your upcoming schedule: $top, sir.")
                                } else {
                                    viewModel.voiceManager.speak("Your schedule is currently clear, sir.")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("read_schedule_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Briefing", color = ArcCyanGlow, fontSize = 11.sp)
                        }
                    }
                }
            }

            // Next Event Hero
            if (nextEvent != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, StarkGold.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                            .testTag("next_event_hero_card"),
                        colors = CardDefaults.cardColors(containerColor = JarvisSurfaceVariant),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = StarkGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "NEXT ENGAGEMENT",
                                        color = StarkGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                Text(
                                    text = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault()).format(Date(nextEvent.startTimeMillis)),
                                    color = ArcCyanGlow,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Text(
                                text = nextEvent.title,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (nextEvent.description.isNotBlank()) {
                                Text(
                                    text = nextEvent.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            if (nextEvent.location.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = ArcCyanDim,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = nextEvent.location,
                                        color = ArcCyanDim,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "UPCOMING SCHEDULE TIMELINE",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            items(events, key = { it.id }) { event ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderCyan, RoundedCornerShape(12.dp))
                        .testTag("event_item_${event.id}"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = event.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (event.isJarvisScheduled) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(ArcCyan.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.SmartToy,
                                                contentDescription = null,
                                                tint = ArcCyanGlow,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "JARVIS",
                                                color = ArcCyanGlow,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteCalendarEvent(event.id) },
                                    modifier = Modifier
                                        .size(28.dp)
                                        .testTag("delete_event_${event.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Event",
                                        tint = JarvisRedAlert.copy(alpha = 0.75f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = SimpleDateFormat("h:mm a • EEE, MMM d", Locale.getDefault()).format(Date(event.startTimeMillis)),
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )

                            if (event.location.isNotBlank()) {
                                Text(
                                    text = event.location,
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (event.description.isNotBlank()) {
                            Text(
                                text = event.description,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // FAB to schedule event
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = ArcCyan,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("schedule_event_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Schedule Event")
        }

        // Add Event Dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        text = "SCHEDULE NEW ENGAGEMENT",
                        color = ArcCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Event Title (e.g. Flight Test, Board Review)") },
                            modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = eventHoursAhead,
                            onValueChange = { eventHoursAhead = it },
                            label = { Text("Hours from now (e.g. 2, 5, 24)") },
                            modifier = Modifier.fillMaxWidth().testTag("event_hours_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = eventLocation,
                            onValueChange = { eventLocation = it },
                            label = { Text("Location (e.g. Workshop Lab 4, Tower)") },
                            modifier = Modifier.fillMaxWidth().testTag("event_location_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = eventDesc,
                            onValueChange = { eventDesc = it },
                            label = { Text("Directive Details") },
                            modifier = Modifier.fillMaxWidth().testTag("event_desc_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (eventTitle.isNotBlank()) {
                                val hours = eventHoursAhead.toIntOrNull() ?: 2
                                val cal = Calendar.getInstance().apply {
                                    add(Calendar.HOUR_OF_DAY, hours)
                                }
                                scope.launch {
                                    viewModel.calendarRepository.scheduleEvent(
                                        title = eventTitle,
                                        description = eventDesc.ifBlank { "Scheduled via JARVIS UI" },
                                        startTimeMillis = cal.timeInMillis,
                                        endTimeMillis = cal.timeInMillis + 3600000L,
                                        location = eventLocation
                                    )
                                    viewModel.voiceManager.speak("Added $eventTitle to your executive schedule, sir.")
                                    showAddDialog = false
                                    eventTitle = ""
                                    eventDesc = ""
                                    eventLocation = ""
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        modifier = Modifier.testTag("submit_schedule_event_button")
                    ) {
                        Text("Confirm Directive", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = JarvisSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
