package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.JarvisSurface
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.voice.JarvisLanguage
import com.example.voice.JarvisVoiceManager
import com.example.voice.JarvisVoicePersona

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceSettingsDialog(
    voiceManager: JarvisVoiceManager,
    onDismiss: () -> Unit
) {
    val currentLang by voiceManager.currentLanguage.collectAsState()
    val currentPersona by voiceManager.currentPersona.collectAsState()
    val pitch by voiceManager.voicePitch.collectAsState()
    val speed by voiceManager.voiceSpeed.collectAsState()
    val isSpeaking by voiceManager.isSpeaking.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ArcCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "JARVIS VOICE & LANGUAGE MODULE",
                    color = ArcCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Voice Personas Section
                Text(
                    text = "SELECT AI VOICE PERSONA:",
                    color = StarkGold,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                JarvisVoicePersona.entries.forEach { persona ->
                    val isSelected = currentPersona == persona
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) ArcCyan else BorderCyan,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                voiceManager.applyPersona(persona)
                            }
                            .testTag("voice_persona_${persona.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) JarvisSurfaceElevated else JarvisSurface
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = persona.language.flag,
                                    fontSize = 22.sp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = persona.title,
                                        color = if (isSelected) ArcCyanGlow else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                                    )
                                    Text(
                                        text = persona.subtitle,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "\"${persona.sampleText}\"",
                                        color = ArcCyanDim,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(ArcCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Language Selection Section
                Text(
                    text = "CALIBRATE SPOKEN LANGUAGE:",
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
                    JarvisLanguage.entries.forEach { lang ->
                        val isSel = currentLang == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) ArcCyan else JarvisSurfaceElevated)
                                .border(1.dp, if (isSel) ArcCyanGlow else BorderCyan, RoundedCornerShape(8.dp))
                                .clickable { voiceManager.setLanguage(lang) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("dialog_lang_${lang.name}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = lang.flag, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = lang.displayName.substringBefore(" ("),
                                    color = if (isSel) Color.Black else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Voice Pitch Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = ArcCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "VOCAL PITCH FREQUENCY",
                                color = ArcCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = String.format("%.2fx", pitch),
                            color = ArcCyanGlow,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = pitch,
                        onValueChange = { voiceManager.setVoicePitch(it) },
                        valueRange = 0.65f..1.35f,
                        colors = SliderDefaults.colors(
                            thumbColor = ArcCyan,
                            activeTrackColor = ArcCyan,
                            inactiveTrackColor = BorderCyan
                        ),
                        modifier = Modifier.testTag("pitch_slider")
                    )
                }

                // Voice Speed Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = StarkGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SPEECH CADENCE RATE",
                                color = StarkGold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = String.format("%.2fx", speed),
                            color = StarkGold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Slider(
                        value = speed,
                        onValueChange = { voiceManager.setVoiceSpeed(it) },
                        valueRange = 0.75f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = StarkGold,
                            activeTrackColor = StarkGold,
                            inactiveTrackColor = BorderCyan
                        ),
                        modifier = Modifier.testTag("speed_slider")
                    )
                }

                // Test voice button
                Button(
                    onClick = {
                        voiceManager.speak(currentLang.greeting)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSpeaking) "Synthesizing Speech..." else "Test Speech Synthesis",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("close_voice_settings_button")
            ) {
                Text("Engage Voice Protocols", color = ArcCyanGlow, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
        },
        containerColor = JarvisSurfaceVariant,
        shape = RoundedCornerShape(16.dp)
    )
}
