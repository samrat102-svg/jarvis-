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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.BorderCyan
import com.example.ui.theme.JarvisGreenSuccess
import com.example.ui.theme.JarvisRedAlert
import com.example.ui.theme.JarvisSurfaceElevated
import com.example.ui.theme.JarvisSurfaceVariant
import com.example.ui.theme.StarkGold
import com.example.ui.viewmodel.JarvisHudTelemetry

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Tune

@Composable
fun JarvisTopHud(
    telemetry: JarvisHudTelemetry,
    onToggleAutoScreening: () -> Unit,
    onToggleDnd: () -> Unit,
    onOpenVoiceSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .background(JarvisSurfaceVariant)
            .border(1.dp, BorderCyan, RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
            .padding(14.dp)
            .testTag("jarvis_top_hud")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Stark Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(JarvisGreenSuccess)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STARK INDUSTRIES",
                        color = ArcCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(JarvisSurfaceElevated)
                            .border(1.dp, ArcCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .clickable { onOpenVoiceSettings() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("open_voice_settings_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = "Voice Calibration", tint = ArcCyanGlow, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VOICE", color = ArcCyanGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = telemetry.activeProtocol,
                        color = StarkGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Diagnostic Telemetry Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Power
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = "Core Output",
                        tint = ArcCyanGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "PWR ${telemetry.powerPercentage}%",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Core Temp
                Text(
                    text = "CORE ${telemetry.coreTempCelsius}°C",
                    color = ArcCyanGlow,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                // Security Clearance
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security",
                        tint = StarkGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = telemetry.securityLevel,
                        color = StarkGold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Quick Toggle Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = telemetry.autoScreeningEnabled,
                    onClick = onToggleAutoScreening,
                    label = {
                        Text(
                            text = if (telemetry.autoScreeningEnabled) "Screening: ACTIVE" else "Screening: OFF",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ArcCyan.copy(alpha = 0.2f),
                        selectedLabelColor = ArcCyanGlow,
                        selectedLeadingIconColor = ArcCyanGlow,
                        containerColor = JarvisSurfaceElevated,
                        labelColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("toggle_auto_screening_chip")
                )

                FilterChip(
                    selected = telemetry.isDndActive,
                    onClick = onToggleDnd,
                    label = {
                        Text(
                            text = if (telemetry.isDndActive) "DND: ENGAGED" else "DND: OFF",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DoNotDisturb,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = JarvisRedAlert.copy(alpha = 0.25f),
                        selectedLabelColor = JarvisRedAlert,
                        selectedLeadingIconColor = JarvisRedAlert,
                        containerColor = JarvisSurfaceElevated,
                        labelColor = Color.Gray
                    ),
                    modifier = Modifier.testTag("toggle_dnd_chip")
                )
            }
        }
    }
}
