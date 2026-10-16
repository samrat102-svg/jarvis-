package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Voicemail
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisGreenSuccess
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.JarvisViewModel

@Composable
fun LiveScreeningDialog(
    viewModel: JarvisViewModel
) {
    val screeningState by viewModel.screeningState.collectAsState()

    if (!screeningState.isScreeningActive) return

    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val alertPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alertPulse"
    )

    Dialog(
        onDismissRequest = { /* Force explicit decision */ },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
                .padding(20.dp)
                .testTag("live_screening_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        2.dp,
                        if (screeningState.isVip) ArcCyan else JarvisRedAlert,
                        RoundedCornerShape(20.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (screeningState.isVip) ArcCyanGlow else JarvisRedAlert,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JARVIS COMMS INTERCEPT",
                                color = if (screeningState.isVip) ArcCyanGlow else JarvisRedAlert,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (screeningState.isVip) ArcCyan.copy(alpha = 0.3f) else JarvisRedAlert.copy(alpha = 0.3f)
                                )
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (screeningState.isVip) "VIP DIRECT" else "INTERCEPTED",
                                color = if (screeningState.isVip) ArcCyanGlow else JarvisRedAlert,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    // Mini Pulsating Arc Reactor
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .scale(alertPulse),
                        contentAlignment = Alignment.Center
                    ) {
                        ArcReactorVisualizer(
                            isSpeaking = true,
                            isListening = false,
                            modifier = Modifier.size(110.dp)
                        )
                    }

                    // Caller Identity
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = screeningState.callerName,
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = screeningState.callerPhone,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Real-time Caller Speech Transcription
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, BorderCyan, RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "CALLER AUDIO TRANSCRIPTION:",
                                color = StarkGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "\"${screeningState.callerSpeech}\"",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // JARVIS Verbal Interception Response
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(JarvisSurfaceVariant)
                            .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "JARVIS VERBAL RESPONSE TO CALLER:",
                                color = ArcCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = screeningState.jarvisScreeningResponse,
                                color = ArcCyanGlow,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // JARVIS Recommendation
                    if (screeningState.recommendation.isNotBlank()) {
                        Text(
                            text = "ASSESSMENT: ${screeningState.recommendation}",
                            color = if (screeningState.isVip) ArcCyanGlow else StarkGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Actions Row
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Accept Call (Green)
                        Button(
                            onClick = { viewModel.acceptScreenedCall() },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisGreenSuccess),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("accept_screened_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Accept & Connect Call", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Take Message (Voicemail)
                            Button(
                                onClick = { viewModel.dismissScreenedCallToVoicemail() },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("voicemail_screened_call_button")
                            ) {
                                Icon(Icons.Default.Voicemail, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Take Message", color = ArcCyanGlow, fontSize = 11.sp)
                            }

                            // Block & Annihilate (Red)
                            Button(
                                onClick = { viewModel.blockAndAnnihilateSpam() },
                                colors = ButtonDefaults.buttonColors(containerColor = JarvisRedAlert),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("block_screened_call_button")
                            ) {
                                Icon(Icons.Default.Block, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Terminate & Block", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
