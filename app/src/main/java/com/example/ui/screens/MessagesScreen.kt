package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import com.example.data.model.MessageCategory
import com.example.data.model.MessageRecord
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MessagesScreen(
    viewModel: JarvisViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.callsMessagesRepository.messages.collectAsState()
    val scope = rememberCoroutineScope()

    var showComposeDialog by remember { mutableStateOf(false) }
    var composeRecipient by remember { mutableStateOf("") }
    var composePhone by remember { mutableStateOf("") }
    var composeText by remember { mutableStateOf("") }

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
            // Header summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JARVIS MESSAGE TRIAGE",
                            color = ArcCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Auto-summarization & Contextual Smart Replies",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.executeVoiceCommand("Read my last message")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("read_all_unread_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = ArcCyanGlow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Read Unread", color = ArcCyanGlow, fontSize = 11.sp)
                    }
                }
            }

            // Message Cards
            items(messages, key = { it.id }) { message ->
                var customReplyText by remember { mutableStateOf("") }
                var isCustomReplying by remember { mutableStateOf(false) }
                var activeReplies by remember { mutableStateOf(message.suggestedReplies) }
                var isRefreshingReplies by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (!message.isRead) ArcCyan else BorderCyan,
                            RoundedCornerShape(12.dp)
                        )
                        .testTag("message_card_${message.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (!message.isRead) JarvisSurfaceVariant else JarvisSurface
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Sender & Time
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (message.category) {
                                                MessageCategory.ALPHA -> ArcCyan.copy(alpha = 0.2f)
                                                MessageCategory.DIRECTIVE -> StarkGold.copy(alpha = 0.2f)
                                                else -> JarvisSurfaceElevated
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mail,
                                        contentDescription = null,
                                        tint = when (message.category) {
                                            MessageCategory.ALPHA -> ArcCyanGlow
                                            MessageCategory.DIRECTIVE -> StarkGold
                                            else -> Color.White
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = message.senderName,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (!message.isRead) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(ArcCyan)
                                            )
                                        }
                                    }
                                    Text(
                                        text = message.senderPhone,
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Category badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (message.category) {
                                            MessageCategory.ALPHA -> ArcCyan.copy(alpha = 0.2f)
                                            MessageCategory.DIRECTIVE -> StarkGold.copy(alpha = 0.2f)
                                            else -> JarvisSurfaceElevated
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = message.category.name,
                                    color = when (message.category) {
                                        MessageCategory.ALPHA -> ArcCyanGlow
                                        MessageCategory.DIRECTIVE -> StarkGold
                                        else -> TextSecondary
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Message Body
                        Text(
                            text = message.body,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        // JARVIS AI Summary Box
                        if (message.jarvisSummary.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisSurfaceElevated)
                                    .border(0.5.dp, BorderCyan, RoundedCornerShape(6.dp))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = ArcCyanGlow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "JARVIS DIGEST: ${message.jarvisSummary}",
                                        color = ArcCyanDim,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        // Reply status if sent
                        if (message.replySent != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JarvisGreenSuccess.copy(alpha = 0.15f))
                                    .padding(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = JarvisGreenSuccess,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Reply sent: \"${message.replySent}\"",
                                    color = JarvisGreenSuccess,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // SMART REPLY SUGGESTIONS SECTION (Single-tap)
                        if (message.isIncoming && message.replySent == null) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "SMART REPLY SUGGESTIONS (SINGLE-TAP):",
                                        color = ArcCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    // Refresh AI replies
                                    Text(
                                        text = if (isRefreshingReplies) "Generating..." else "⚡ Regenerate AI",
                                        color = StarkGold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .clickable {
                                                isRefreshingReplies = true
                                                scope.launch {
                                                    val fresh = viewModel.aiService.generateSmartReplies(
                                                        message.senderName,
                                                        message.body
                                                    )
                                                    activeReplies = fresh
                                                    isRefreshingReplies = false
                                                }
                                            }
                                            .testTag("regenerate_smart_replies_${message.id}")
                                    )
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    activeReplies.forEachIndexed { index, replyText ->
                                        SuggestionChip(
                                            onClick = {
                                                viewModel.selectSmartReply(message, replyText)
                                            },
                                            label = {
                                                Text(
                                                    text = replyText,
                                                    fontSize = 11.sp,
                                                    color = TextPrimary
                                                )
                                            },
                                            colors = SuggestionChipDefaults.suggestionChipColors(
                                                containerColor = JarvisSurfaceElevated
                                            ),
                                            border = SuggestionChipDefaults.suggestionChipBorder(
                                                enabled = true,
                                                borderColor = ArcCyan.copy(alpha = 0.6f)
                                            ),
                                            modifier = Modifier.testTag("smart_reply_chip_${message.id}_$index")
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom Actions: Voice Read Aloud & Manual Reply
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    viewModel.callsMessagesRepository.markMessageRead(message.id)
                                    viewModel.voiceManager.speak("Message from ${message.senderName}: ${message.body}. Summary: ${message.jarvisSummary}")
                                },
                                modifier = Modifier.testTag("speak_message_button_${message.id}")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Read Aloud", color = ArcCyanGlow, fontSize = 11.sp)
                            }

                            TextButton(
                                onClick = { isCustomReplying = !isCustomReplying },
                                modifier = Modifier.testTag("toggle_custom_reply_${message.id}")
                            ) {
                                Text(
                                    text = if (isCustomReplying) "Cancel" else "Custom Reply",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Custom Reply Field
                        AnimatedVisibility(visible = isCustomReplying) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = customReplyText,
                                    onValueChange = { customReplyText = it },
                                    placeholder = { Text("Type reply...", fontSize = 12.sp, color = TextSecondary) },
                                    modifier = Modifier.weight(1f).testTag("custom_reply_input_${message.id}"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = ArcCyan,
                                        unfocusedBorderColor = BorderCyan,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )

                                Button(
                                    onClick = {
                                        if (customReplyText.isNotBlank()) {
                                            viewModel.selectSmartReply(message, customReplyText)
                                            customReplyText = ""
                                            isCustomReplying = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("send_custom_reply_${message.id}")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }

        // FAB to compose new message
        FloatingActionButton(
            onClick = { showComposeDialog = true },
            containerColor = ArcCyan,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("compose_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Compose Message")
        }

        // Compose Dialog
        if (showComposeDialog) {
            AlertDialog(
                onDismissRequest = { showComposeDialog = false },
                title = {
                    Text(
                        text = "NEW TRANSMISSION DISPATCH",
                        color = ArcCyan,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = composeRecipient,
                            onValueChange = { composeRecipient = it },
                            label = { Text("Recipient Name (e.g. Pepper, Rhodey)") },
                            modifier = Modifier.fillMaxWidth().testTag("compose_recipient_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = composePhone,
                            onValueChange = { composePhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().testTag("compose_phone_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ArcCyan,
                                unfocusedBorderColor = BorderCyan,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = composeText,
                            onValueChange = { composeText = it },
                            label = { Text("Message Body") },
                            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("compose_body_input"),
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
                            if (composeRecipient.isNotBlank() && composeText.isNotBlank()) {
                                viewModel.callsMessagesRepository.composeNewMessage(
                                    composeRecipient,
                                    composePhone.ifBlank { "+1 (555) 000-0000" },
                                    composeText
                                )
                                viewModel.executeVoiceCommand("Send text to $composeRecipient saying $composeText")
                                showComposeDialog = false
                                composeRecipient = ""
                                composePhone = ""
                                composeText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        modifier = Modifier.testTag("submit_compose_button")
                    ) {
                        Text("Dispatch Transmission", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showComposeDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                },
                containerColor = JarvisSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
