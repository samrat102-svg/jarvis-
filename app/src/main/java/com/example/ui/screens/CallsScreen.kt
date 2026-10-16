package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallRecord
import com.example.data.model.CallType
import com.example.data.model.JarvisIntentTag
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanDim
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.JarvisGreenSuccess
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallsScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val calls by viewModel.callsMessagesRepository.calls.collectAsState()

    var expandedCallId by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card: Live Incoming Call Screener Simulator
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .testTag("call_screener_hero_card"),
                colors = CardDefaults.elevatedCardColors(containerColor = JarvisSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = ArcCyan,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JARVIS CALL SCREENER",
                                color = ArcCyan,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Text(
                            text = "LIVE PROTOCOL",
                            color = StarkGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "JARVIS automatically intercepts unknown and incoming calls, transcribes caller audio, verifies intent, and presents real-time recommendations.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "TEST LIVE CALL SCREENING:",
                        color = ArcCyanGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.simulateIncomingCall(
                                    callerName = "Telemarketer (Global Solar)",
                                    callerPhone = "+1 (800) 555-0149",
                                    sampleSpeech = "Hello, this is Brenda calling with a zero-down clean energy financing offer for Stark Tower residents.",
                                    isVip = false
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert.copy(alpha = 0.85f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("simulate_spam_call_button")
                        ) {
                            Icon(Icons.Default.PhoneInTalk, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Spam Telemarketer", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.simulateIncomingCall(
                                    callerName = "Pepper Potts",
                                    callerPhone = "+1 (555) 782-7501",
                                    sampleSpeech = "Tony, the board meeting is being pushed forward 30 minutes. Are you in the city or flying?",
                                    isVip = true
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("simulate_vip_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pepper Potts (VIP)", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                viewModel.simulateIncomingCall(
                                    callerName = "Col. James Rhodes",
                                    callerPhone = "+1 (555) 927-4402",
                                    sampleSpeech = "Tony, satellite feed picked up anomalous seismic readings off the coast. Need armor support.",
                                    isVip = true
                                )
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = JarvisSurfaceElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("simulate_rhodey_call_button")
                        ) {
                            Text("Col. Rhodes (Urgent)", color = StarkGold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Call Logs Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMUNICATIONS LOG & TRANSCRIPTS",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "${calls.size} RECORDS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Call Records
        items(calls, key = { it.id }) { call ->
            val isExpanded = expandedCallId == call.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, if (call.isSpam) JarvisRedAlert.copy(alpha = 0.5f) else BorderCyan, RoundedCornerShape(12.dp))
                    .clickable {
                        expandedCallId = if (isExpanded) null else call.id
                    }
                    .testTag("call_item_${call.id}"),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            call.isSpam -> JarvisRedAlert.copy(alpha = 0.2f)
                                            call.intentTag == JarvisIntentTag.VIP -> ArcCyan.copy(alpha = 0.2f)
                                            else -> JarvisSurfaceElevated
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (call.callType) {
                                        CallType.INCOMING -> Icons.AutoMirrored.Filled.CallReceived
                                        CallType.OUTGOING -> Icons.AutoMirrored.Filled.CallMade
                                        CallType.SCREENED -> Icons.Default.Security
                                        CallType.MISSED -> Icons.Default.CallEnd
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        call.isSpam -> JarvisRedAlert
                                        call.intentTag == JarvisIntentTag.VIP -> ArcCyanGlow
                                        else -> Color.White
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = call.contactName,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = call.phoneNumber,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            // Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (call.intentTag) {
                                            JarvisIntentTag.VIP -> ArcCyan.copy(alpha = 0.25f)
                                            JarvisIntentTag.SPAM -> JarvisRedAlert.copy(alpha = 0.25f)
                                            JarvisIntentTag.BUSINESS -> StarkGold.copy(alpha = 0.25f)
                                            JarvisIntentTag.URGENT -> JarvisRedAlert.copy(alpha = 0.35f)
                                            JarvisIntentTag.CASUAL -> JarvisSurfaceElevated
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = call.intentTag.name,
                                    color = when (call.intentTag) {
                                        JarvisIntentTag.VIP -> ArcCyanGlow
                                        JarvisIntentTag.SPAM -> JarvisRedAlert
                                        JarvisIntentTag.BUSINESS -> StarkGold
                                        JarvisIntentTag.URGENT -> JarvisRedAlert
                                        JarvisIntentTag.CASUAL -> TextSecondary
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(call.timestamp)),
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Assessment snippet
                    if (call.jarvisAssessment.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(JarvisSurfaceElevated)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "JARVIS: ${call.jarvisAssessment}",
                                color = ArcCyanDim,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Expanded Details (Transcript & Actions)
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            if (call.transcript.isNotBlank()) {
                                Text(
                                    text = "TRANSCRIPT INTERCEPTION:",
                                    color = ArcCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = call.transcript,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Callback button
                                Button(
                                    onClick = {
                                        viewModel.executeVoiceCommand("Call ${call.contactName}")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("callback_button_${call.id}")
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Callback", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // SMS button
                                FilledTonalButton(
                                    onClick = {
                                        try {
                                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                                data = Uri.parse("smsto:${Uri.encode(call.phoneNumber)}")
                                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                            }
                                            context.startActivity(smsIntent)
                                        } catch (e: Exception) {
                                            // ignore
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = JarvisSurfaceElevated),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("sms_button_${call.id}")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Message", color = ArcCyanGlow, fontSize = 11.sp)
                                }

                                // Delete Call button
                                IconButton(
                                    onClick = {
                                        viewModel.deleteCallRecord(call.id)
                                    },
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(JarvisRedAlert.copy(alpha = 0.15f))
                                        .testTag("delete_call_${call.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Call", tint = JarvisRedAlert, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Expand / Collapse Chevron indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
