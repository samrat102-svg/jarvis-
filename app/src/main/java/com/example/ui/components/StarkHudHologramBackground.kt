package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.JarvisBackground
import com.example.ui.theme.StarkGold

@Composable
fun StarkHudHologramBackground(
    modifier: Modifier = Modifier,
    isVoiceActive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "StarkHologramBackground")

    // Vertical scanline sweep
    val scanlineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scanlineY"
    )

    // Subtle ambient grid pulse
    val gridAlpha by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = if (isVoiceActive) 0.22f else 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gridAlpha"
    )

    // Radial central glow pulsation
    val radialPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radialPulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Deep Space Void Base
        drawRect(color = JarvisBackground)

        // Center Arc Reactor Ambient Radial Glow
        val center = Offset(w / 2f, h * 0.38f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ArcCyan.copy(alpha = if (isVoiceActive) 0.30f else 0.16f),
                    ArcCyanGlow.copy(alpha = if (isVoiceActive) 0.15f else 0.06f),
                    Color.Transparent
                ),
                center = center,
                radius = (w * 0.75f * radialPulse).coerceAtLeast(100f)
            ),
            radius = w * 0.75f * radialPulse,
            center = center
        )

        // Subtle Holographic Coordinate Grid
        val gridSize = 48f
        var x = 0f
        while (x < w) {
            drawLine(
                color = ArcCyan.copy(alpha = gridAlpha * 0.5f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 0.8f
            )
            x += gridSize
        }

        var y = 0f
        while (y < h) {
            drawLine(
                color = ArcCyan.copy(alpha = gridAlpha * 0.5f),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 0.8f
            )
            y += gridSize
        }

        // Horizontal Moving Scanline with Glow Trail
        val currentScanY = h * scanlineY
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    ArcCyanGlow.copy(alpha = 0.65f),
                    ArcCyan.copy(alpha = 0.85f),
                    ArcCyanGlow.copy(alpha = 0.65f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, currentScanY),
            end = Offset(w, currentScanY),
            strokeWidth = 1.8f
        )

        // Scanline faint gradient trail
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    ArcCyan.copy(alpha = 0.08f),
                    Color.Transparent
                ),
                startY = (currentScanY - 60f).coerceAtLeast(0f),
                endY = currentScanY
            ),
            topLeft = Offset(0f, (currentScanY - 60f).coerceAtLeast(0f)),
            size = androidx.compose.ui.geometry.Size(w, 60f)
        )

        // Subtle Cyber Corner Hex Nodes
        val hexNodes = listOf(
            Offset(24f, 40f),
            Offset(w - 24f, 40f),
            Offset(24f, h - 60f),
            Offset(w - 24f, h - 60f)
        )
        for (node in hexNodes) {
            drawCircle(
                color = StarkGold.copy(alpha = 0.6f),
                radius = 2.5f,
                center = node
            )
            drawCircle(
                color = ArcCyan.copy(alpha = 0.3f),
                radius = 6f,
                center = node,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
            )
        }
    }
}
