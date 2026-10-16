package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Tune
import com.example.ui.components.VoiceBiometricsDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentType
import com.example.ui.components.ArcReactorVisualizer
import com.example.ui.components.AudioWaveformBar
import com.example.ui.components.JarvisTopHud
import com.example.ui.components.StarkHudHologramBackground
import com.example.ui.components.VoiceSettingsDialog
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
import com.example.ui.viewmodel.JarvisScreen
import com.example.ui.viewmodel.JarvisViewModel
import com.example.voice.JarvisLanguage
import com.example.voice.JarvisVoiceManager
import com.example.voice.JarvisVoicePersona

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: JarvisViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    onLaunchSpeechDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val telemetry by viewModel.telemetry.collectAsState()
    val jarvisNotice by viewModel.jarvisNotice.collectAsState()
    val history by viewModel.voiceCommandHistory.collectAsState()
    val isSpeaking by viewModel.voiceManager.isSpeaking.collectAsState()
    val isListening by viewModel.voiceManager.isListening.collectAsState()
    val audioRms by viewModel.voiceManager.audioRms.collectAsState()
    val speechError by viewModel.voiceManager.speechError.collectAsState()
    val isProcessingAi by viewModel.isProcessingAi.collectAsState()
    val currentLang by viewModel.voiceManager.currentLanguage.collectAsState()
    val currentPersona by viewModel.voiceManager.currentPersona.collectAsState()
    val isGeneratingDoc by viewModel.isGeneratingDocument.collectAsState()

    var manualInputText by remember { mutableStateOf("") }
    var showVoiceSettings by remember { mutableStateOf(false) }
    var showBiometricsDialog by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "HomeGlowingAnim")
    val heroGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroGlowAlpha"
    )

    val quickCommands = if (currentLang == JarvisLanguage.HINDI) {
        listOf(
            "हे जारविस, व्हाट्सएप खोलो",
            "हे जारविस, पेप्पर को 50 डॉलर भेजो",
            "हे जारविस, मेरी आवाज़ पहचानो",
            "हे जारविस, कॉल डेटा डिलीट करो",
            "हे जारविस, जॉन का नंबर हटाओ",
            "हे जारविस, कैलेंडर डेटा मिटाओ",
            "हे जारविस, मैसेज पढ़ो",
            "हे जारविस, मंगल ग्रह पर पीपीटी बनाओ",
            "हे जारविस, डिफेंस बजट पर एक्सेल बनाओ",
            "हे जारविस, हालचाल बताओ"
        )
    } else {
        listOf(
            "Hey Jarvis, open WhatsApp",
            "Hey Jarvis, launch Camera",
            "Hey Jarvis, pay $50 to Pepper Potts",
            "Hey Jarvis, who is speaking",
            "Hey Jarvis, delete number John",
            "Hey Jarvis, delete call data",
            "Hey Jarvis, clear calendar data",
            "Hey Jarvis, read my last message",
            "Hey Jarvis, make a PPT on Arc Reactor Energy",
            "Hey Jarvis, status report"
        )
    }

    if (showVoiceSettings) {
        VoiceSettingsDialog(
            voiceManager = viewModel.voiceManager,
            onDismiss = { showVoiceSettings = false }
        )
    }

    if (showBiometricsDialog) {
        VoiceBiometricsDialog(
            viewModel = viewModel,
            onDismiss = { showBiometricsDialog = false }
        )
    }

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                JarvisTopHud(
                    telemetry = telemetry,
                    onToggleAutoScreening = { viewModel.toggleAutoScreening() },
                    onToggleDnd = { viewModel.toggleDnd() },
                    onOpenVoiceSettings = { showVoiceSettings = true }
                )
            }

            // Glowing Signature Voice Initialization Hero Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    ArcCyan.copy(alpha = heroGlowAlpha),
                                    StarkGold.copy(alpha = heroGlowAlpha),
                                    ArcCyanGlow.copy(alpha = heroGlowAlpha)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .testTag("signature_voice_hero_banner"),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
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
                                        .background(ArcCyan.copy(alpha = 0.2f))
                                        .border(1.dp, ArcCyan, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = ArcCyanGlow,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "STARK SIGNATURE VOCAL PROTOCOL",
                                        color = ArcCyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        text = "Iron Man AI Butler Audio Synchronized",
                                        color = StarkGold,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            // Glowing Live Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ArcCyan.copy(alpha = 0.15f))
                                    .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (isSpeaking) "● TRANSMITTING" else "● SYNCHRONIZED",
                                    color = if (isSpeaking) StarkGold else ArcCyanGlow,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Text(
                            text = "\"Greetings. I've detected your presence and initialized all primary systems. My protocols are fully synchronized with your personal preferences...\"",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp,
                            fontFamily = FontFamily.SansSerif
                        )

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    viewModel.playSignatureGreeting()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("play_signature_greeting_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSpeaking) StarkGold else ArcCyan
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isSpeaking) "Stop Speech" else "Play Signature Voice",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Button(
                                onClick = { showVoiceSettings = true },
                                modifier = Modifier.testTag("tune_voice_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JarvisSurface
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderCyan),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = ArcCyanGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tune",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }

            // Center Arc Reactor Visualizer & Reactive Moving Waveform
            item {
                Spacer(modifier = Modifier.height(2.dp))
                ArcReactorVisualizer(
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    audioRms = audioRms,
                    onClick = {
                        if (isListening) {
                            viewModel.voiceManager.stopListening()
                        } else {
                            onRequestRecordAudioPermission()
                            val started = viewModel.voiceManager.startListening()
                            if (!started) {
                                onLaunchSpeechDialog()
                            }
                        }
                    }
                )

                // Reactive Audio Waveform Indicator
                AudioWaveformBar(
                    isSpeaking = isSpeaking,
                    isListening = isListening,
                    audioRms = audioRms
                )

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isListening) "LISTENING: SPEAK COMMAND OR QUERY..." else if (isSpeaking) "JARVIS VOCAL SYNTHESIS ACTIVE..." else "TAP REACTOR FOR VOICE DIRECTIVE",
                    color = if (isListening) StarkGold else ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            // Quick Persona & Language Switcher Bar
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderCyan, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = ArcCyanGlow,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "VOICE MODULE: ${currentPersona.title.uppercase()}",
                                    color = ArcCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = "CALIBRATE →",
                                color = StarkGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.clickable { showVoiceSettings = true }
                            )
                        }

                        // Horizontal Persona Selector
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            JarvisVoicePersona.entries.forEach { persona ->
                                val isSel = currentPersona == persona
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) ArcCyan else JarvisSurfaceElevated)
                                        .border(1.dp, if (isSel) ArcCyanGlow else BorderCyan, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.voiceManager.applyPersona(persona) }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                        .testTag("quick_persona_${persona.id}")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = persona.language.flag, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = persona.title,
                                            color = if (isSel) Color.Black else TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Command Input Box & Mic Action
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderCyan, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.Mic,
                                    contentDescription = null,
                                    tint = ArcCyanGlow,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "INTELLIGENT COMMAND INTERFACE",
                                    color = ArcCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }

                            if (isSpeaking) {
                                IconButton(
                                    onClick = { viewModel.voiceManager.stopSpeaking() },
                                    modifier = Modifier.size(28.dp).testTag("stop_speaking_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop Speech",
                                        tint = JarvisRedAlert
                                    )
                                }
                            }
                        }

                        // Speech & Status Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(JarvisSurfaceElevated)
                                .border(1.dp, ArcCyanDim.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            if (isProcessingAi || isGeneratingDoc) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = ArcCyan,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isGeneratingDoc) "Compiling Stark Intelligence Document..." else "Processing AI Neural Query...",
                                        color = ArcCyanGlow,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            } else {
                                Text(
                                    text = jarvisNotice,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    fontFamily = FontFamily.SansSerif
                                )
                            }
                        }

                        if (speechError != null) {
                            Text(
                                text = speechError ?: "",
                                color = JarvisRedAlert,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Input Field & Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = manualInputText,
                                onValueChange = { manualInputText = it },
                                placeholder = {
                                    Text(
                                        text = if (currentLang == JarvisLanguage.HINDI) "कमांड टाइप करें या बोलें..." else "Say or type: 'Call Mom', 'Make a PPT on AI'...",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("command_input_field"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ArcCyan,
                                    unfocusedBorderColor = BorderCyan,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = ArcCyanGlow
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Send typed command
                            IconButton(
                                onClick = {
                                    if (manualInputText.isNotBlank()) {
                                        viewModel.executeVoiceCommand(manualInputText)
                                        manualInputText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ArcCyan)
                                    .testTag("send_command_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send Command",
                                    tint = Color.Black
                                )
                            }

                            // Mic Toggle Button
                            IconButton(
                                onClick = {
                                    if (isListening) {
                                        viewModel.voiceManager.stopListening()
                                    } else {
                                        onRequestRecordAudioPermission()
                                        val started = viewModel.voiceManager.startListening()
                                        if (!started) {
                                            onLaunchSpeechDialog()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(if (isListening) StarkGold else JarvisSurfaceElevated)
                                    .border(1.5.dp, if (isListening) StarkGold else ArcCyan, CircleShape)
                                    .testTag("mic_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = if (isListening) Color.Black else ArcCyanGlow
                                )
                            }
                        }
                    }
                }
            }

            // STARK TACTICAL NEXUS: App Access, Voice Recognition, Voice Pay, Data Eraser
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderCyan, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STARK TACTICAL NEXUS & CONTROLS",
                                    color = ArcCyanGlow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }
                            Text(
                                text = "MARK LXXXV",
                                color = StarkGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Row 1: App Access Nexus & Voice Biometrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Card 1: App Access
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.navigateTo(JarvisScreen.Apps) }
                                    .testTag("nexus_app_access_card"),
                                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Apps, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("APP ACCESS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Open Any App • Voice Launch", color = ArcCyanDim, fontSize = 10.sp)
                                }
                            }

                            // Card 2: Voice Recognition Biometrics
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, StarkGold.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { showBiometricsDialog = true }
                                    .testTag("nexus_voice_biometrics_card"),
                                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = StarkGold, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("VOICE RECOGNITION", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Speaker ID • OMEGA-1 Auth", color = StarkGold.copy(alpha = 0.8f), fontSize = 10.sp)
                                }
                            }
                        }

                        // Row 2: Voice Pay & Data Vault
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Card 3: Voice Pay Terminal
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, JarvisGreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.navigateTo(JarvisScreen.VoicePay) }
                                    .testTag("nexus_voice_pay_card"),
                                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.Payment, contentDescription = null, tint = JarvisGreenSuccess, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("VOICE PAY", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Transfer by Voice • ArcPay", color = JarvisGreenSuccess, fontSize = 10.sp)
                                }
                            }

                            // Card 4: Data Vault & Eraser
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, JarvisRedAlert.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.navigateTo(JarvisScreen.DataVault) }
                                    .testTag("nexus_data_vault_card"),
                                colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = JarvisRedAlert, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("DATA VAULT", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text("Delete Numbers, Calls, Cal", color = JarvisRedAlert.copy(alpha = 0.85f), fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Document Creation Matrix
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderCyan, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurface),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = StarkGold, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "STARK DOCUMENT & AI INTELLIGENCE",
                                    color = StarkGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    letterSpacing = 1.sp
                                )
                            }

                            Text(
                                text = "VIEW ALL →",
                                color = ArcCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.clickable { viewModel.navigateTo(JarvisScreen.Documents) }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisSurfaceElevated)
                                    .border(1.dp, StarkGold.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.createDocument(DocumentType.PPT, "Mark VII Armor Telemetry & Flight Deck")
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Slideshow, contentDescription = null, tint = StarkGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PPT Slides", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisSurfaceElevated)
                                    .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.createDocument(DocumentType.PDF, "Comprehensive Comms Audit & Threat Report")
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("PDF Report", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisSurfaceElevated)
                                    .border(1.dp, JarvisGreenSuccess.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.createDocument(DocumentType.EXCEL, "Stark Defense Budget & Variance Sheet")
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.TableChart, contentDescription = null, tint = JarvisGreenSuccess, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Excel Matrix", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(JarvisSurfaceElevated)
                                    .border(1.dp, ArcCyanGlow.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .clickable {
                                        viewModel.createDocument(DocumentType.WORD, "Executive Operational Memorandum")
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Word Memo", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Directive Suggestions Chips
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "QUICK VOICE PROTOCOLS",
                        color = ArcCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickCommands.forEach { cmd ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.executeVoiceCommand(cmd)
                                },
                                label = {
                                    Text(
                                        text = cmd,
                                        fontSize = 11.sp,
                                        color = TextPrimary
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = JarvisSurfaceElevated
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = BorderCyan
                                ),
                                modifier = Modifier.testTag("quick_cmd_${cmd.take(10).replace(" ", "_")}")
                            )
                        }
                    }
                }
            }

            // Live Comms Terminal Log
            item {
                Text(
                    text = "COMMUNICATIONS & DIRECTIVE FEED",
                    color = ArcCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            items(history) { logEntry ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurface)
                        .border(1.dp, BorderCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = logEntry,
                        color = if (logEntry.startsWith("JARVIS:")) ArcCyanGlow else if (logEntry.startsWith("Command:")) StarkGold else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
}
