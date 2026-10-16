package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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

@Composable
fun VoiceBiometricsDialog(
    viewModel: JarvisViewModel,
    onDismiss: () -> Unit
) {
    val profile by viewModel.voiceBiometricsManager.profile.collectAsState()
    val lastRecognition by viewModel.voiceBiometricsManager.lastRecognition.collectAsState()

    var commanderNameInput by remember { mutableStateOf(profile.commanderName) }
    var enrolledPhraseInput by remember { mutableStateOf(profile.enrolledPhrase) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.5.dp, ArcCyanGlow, RoundedCornerShape(16.dp))
                .testTag("voice_biometrics_dialog"),
            colors = CardDefaults.cardColors(containerColor = JarvisSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(ArcCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = ArcCyanGlow, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "VOICE RECOGNITION BIOMETRICS",
                                color = ArcCyanGlow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Speaker Identification & Clearance Level",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                // Active Speaker Match Badge
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (lastRecognition.isAuthorized) JarvisGreenSuccess else JarvisRedAlert,
                            RoundedCornerShape(10.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = JarvisSurfaceElevated),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (lastRecognition.isAuthorized) Icons.Default.Shield else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (lastRecognition.isAuthorized) JarvisGreenSuccess else JarvisRedAlert,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = lastRecognition.speakerName,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Confidence: ${lastRecognition.confidencePercent}% • Clearance: ${lastRecognition.clearanceLevel}",
                                    color = if (lastRecognition.isAuthorized) ArcCyanGlow else JarvisRedAlert,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (lastRecognition.isAuthorized) JarvisGreenSuccess.copy(alpha = 0.2f) else JarvisRedAlert.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (lastRecognition.isAuthorized) "VERIFIED" else "UNAUTHORIZED",
                                color = if (lastRecognition.isAuthorized) JarvisGreenSuccess else JarvisRedAlert,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Speaker Identification Simulator
                Text(
                    text = "SPEAKER RECOGNITION SIMULATION",
                    color = ArcCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val res = viewModel.voiceBiometricsManager.verifySpeaker("Jarvis execute command", isSimulatedGuest = false)
                            viewModel.voiceManager.speak("Voice recognized: ${res.speakerName}. Match verified at ${res.confidencePercent} percent.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Commander", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val res = viewModel.voiceBiometricsManager.verifySpeaker("Open payment protocol", isSimulatedGuest = true)
                            viewModel.voiceManager.speak("Voice mismatch. Unenrolled frequency detected. Clearance level guest.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisSurfaceElevated),
                        border = androidx.compose.foundation.BorderStroke(1.dp, JarvisRedAlert.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Test Guest Voice", color = JarvisRedAlert, fontSize = 11.sp)
                    }
                }

                // Commander Profile Inputs
                OutlinedTextField(
                    value = commanderNameInput,
                    onValueChange = {
                        commanderNameInput = it
                        viewModel.voiceBiometricsManager.setCommanderName(it)
                    },
                    label = { Text("Commander Identifier", color = TextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = enrolledPhraseInput,
                    onValueChange = {
                        enrolledPhraseInput = it
                        viewModel.voiceBiometricsManager.setEnrolledPhrase(it)
                    },
                    label = { Text("Enrolled Passkey Phrase", color = TextSecondary, fontSize = 11.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcCyan,
                        unfocusedBorderColor = BorderCyan,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Strict Voice Security Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JarvisSurfaceElevated)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Strict Voice Security Protocol", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Require Commander voice match for Voice Payments & Data Deletion", color = TextSecondary, fontSize = 10.sp)
                    }

                    Switch(
                        checked = profile.strictSecurityEnabled,
                        onCheckedChange = { viewModel.voiceBiometricsManager.setStrictSecurity(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = ArcCyanGlow,
                            checkedTrackColor = ArcCyanDim
                        )
                    )
                }

                // Close Button
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ArcCyan),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save & Calibrate Voice Profile", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
