package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.StarkGold
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ArcReactorVisualizer(
    isSpeaking: Boolean,
    isListening: Boolean,
    audioRms: Float = 0f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ArcReactorLive")

    // Slow majestic outer ring rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outerRotation"
    )

    // Reverse middle telemetry rotation
    val middleRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "middleRotation"
    )

    // Inner coil fast rotation
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "innerRotation"
    )

    // Dynamic energy pulse
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = if (isSpeaking || isListening) 1.15f else 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 550 else 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "corePulse"
    )

    // Expanding radial light ripple
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking || isListening) 1000 else 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )

    // Orbiting particle angle
    val particleAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleAngle"
    )

    Box(
        modifier = modifier
            .size(260.dp)
            .testTag("arc_reactor_visualizer")
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = (size.minDimension / 2f) * 0.90f
            val maxRadius = baseRadius * corePulse

            // 1. Expanding Holographic Energy Ripple (Emitting from Core)
            drawCircle(
                color = ArcCyan.copy(alpha = ((1f - (rippleScale / 1.25f)) * 0.45f).coerceIn(0f, 0.45f)),
                radius = baseRadius * rippleScale,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 2. Large Neon Cyan Bloom Backdrop
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ArcCyan.copy(alpha = if (isListening) 0.55f else if (isSpeaking) 0.65f else 0.28f),
                        ArcCyanGlow.copy(alpha = if (isListening) 0.28f else if (isSpeaking) 0.35f else 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = maxRadius * 1.20f
                ),
                radius = maxRadius * 1.20f,
                center = center
            )

            // 3. Outermost Thin Precision HUD Track
            drawCircle(
                color = ArcCyan.copy(alpha = 0.35f),
                radius = maxRadius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 4. Rotating Outer Telemetry Tick Segments
            rotate(outerRotation, pivot = center) {
                val tickCount = 36
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (Math.PI / 180f)
                    val r1 = maxRadius - 7.dp.toPx()
                    val r2 = maxRadius + 3.dp.toPx()
                    val start = Offset(
                        center.x + (r1 * cos(angle)).toFloat(),
                        center.y + (r1 * sin(angle)).toFloat()
                    )
                    val end = Offset(
                        center.x + (r2 * cos(angle)).toFloat(),
                        center.y + (r2 * sin(angle)).toFloat()
                    )
                    val isMajor = i % 4 == 0
                    drawLine(
                        color = if (isMajor) StarkGold else ArcCyan.copy(alpha = 0.65f),
                        start = start,
                        end = end,
                        strokeWidth = if (isMajor) 2.5.dp.toPx() else 1.2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 5. Middle Glowing Segmented Arc Ring (Reverse Rotation)
            val middleRadius = maxRadius * 0.74f
            drawCircle(
                color = ArcCyan.copy(alpha = 0.45f),
                radius = middleRadius,
                center = center,
                style = Stroke(width = 2.5.dp.toPx())
            )

            rotate(middleRotation, pivot = center) {
                // 4 glowing heavy segment blocks
                val blockCount = 4
                for (b in 0 until blockCount) {
                    val startAngle = b * 90f + 15f
                    drawArc(
                        color = if (isSpeaking) ArcCyanGlow else if (isListening) StarkGold else ArcCyan,
                        startAngle = startAngle,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(center.x - middleRadius, center.y - middleRadius),
                        size = androidx.compose.ui.geometry.Size(middleRadius * 2, middleRadius * 2),
                        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 6. Orbiting Glowing Energy Sparks (Moving Nodes)
            val orbitRadius = maxRadius * 0.86f
            val sparkCount = 4
            for (s in 0 until sparkCount) {
                val sparkRad = ((particleAngle + s * (360f / sparkCount)) * (Math.PI / 180f))
                val sx = center.x + (orbitRadius * cos(sparkRad)).toFloat()
                val sy = center.y + (orbitRadius * sin(sparkRad)).toFloat()

                // Spark halo
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            if (s % 2 == 0) ArcCyanGlow else StarkGold,
                            Color.Transparent
                        ),
                        center = Offset(sx, sy),
                        radius = 12.dp.toPx()
                    ),
                    radius = 12.dp.toPx(),
                    center = Offset(sx, sy)
                )

                // Spark dot
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(sx, sy)
                )
            }

            // 7. Middle Induction Energy Coils Ring
            val coilRingRadius = maxRadius * 0.52f
            drawCircle(
                color = ArcCyan.copy(alpha = 0.6f),
                radius = coilRingRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            rotate(innerRotation, pivot = center) {
                val coilCount = 10
                for (i in 0 until coilCount) {
                    val angle = (i * 360f / coilCount) * (Math.PI / 180f)
                    val cx = center.x + (coilRingRadius * cos(angle)).toFloat()
                    val cy = center.y + (coilRingRadius * sin(angle)).toFloat()

                    drawCircle(
                        color = StarkGold,
                        radius = 3.5.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 1.8.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // 8. Inner Core Chamber Chamber
            val coreRingRadius = maxRadius * 0.38f
            drawCircle(
                color = ArcCyanGlow,
                radius = coreRingRadius,
                center = center,
                style = Stroke(width = 2.8.dp.toPx())
            )

            // 9. Central Radiant Fusion Triangle (Mark LXXXV Core)
            val triSize = coreRingRadius * 0.90f
            val triPath = Path().apply {
                val h = triSize * (Math.sqrt(3.0) / 2.0).toFloat()
                moveTo(center.x, center.y - (2f / 3f * h))
                lineTo(center.x + triSize / 2f, center.y + (1f / 3f * h))
                lineTo(center.x - triSize / 2f, center.y + (1f / 3f * h))
                close()
            }

            // Glowing Radial Core Fill
            drawPath(
                path = triPath,
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        ArcCyanGlow,
                        ArcCyan.copy(alpha = 0.85f)
                    ),
                    center = center,
                    radius = triSize * 0.95f
                )
            )

            // Crisp White Outline on Triangle Core
            drawPath(
                path = triPath,
                color = Color.White,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Pure White Blinding Core Point
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, ArcCyanGlow, Color.Transparent),
                    center = center,
                    radius = 10.dp.toPx()
                ),
                radius = 10.dp.toPx(),
                center = center
            )
            drawCircle(
                color = Color.White,
                radius = 4.5.dp.toPx(),
                center = center
            )
        }

        // Status Label with glowing cyber text
        Text(
            text = when {
                isListening -> "● LISTENING..."
                isSpeaking -> "● VOICE ACTIVE"
                else -> "● ARC REACTOR ONLINE"
            },
            color = if (isListening) StarkGold else ArcCyanGlow,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
