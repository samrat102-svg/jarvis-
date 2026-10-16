package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ArcCyan
import com.example.ui.theme.ArcCyanGlow
import com.example.ui.theme.StarkGold
import kotlin.random.Random

@Composable
fun AudioWaveformBar(
    isSpeaking: Boolean,
    isListening: Boolean,
    audioRms: Float = 0f,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WaveformAnim")

    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isSpeaking || isListening) 0.95f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim1"
    )

    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isSpeaking || isListening) 0.85f else 0.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim2"
    )

    val anim3 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = if (isSpeaking || isListening) 1.0f else 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "anim3"
    )

    val barCount = 20
    val rmsFactor = (audioRms / 10f).coerceIn(0.1f, 1.2f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val baseHeight = when (i % 4) {
                0 -> anim1
                1 -> anim2
                2 -> anim3
                else -> (anim1 + anim2) / 2f
            }
            val heightFraction = if (isSpeaking || isListening) {
                (baseHeight * rmsFactor).coerceIn(0.2f, 1.0f)
            } else {
                (baseHeight * 0.4f).coerceIn(0.15f, 0.4f)
            }

            val barColor = if (isListening) {
                if (i % 2 == 0) StarkGold else ArcCyan
            } else if (isSpeaking) {
                if (i % 3 == 0) ArcCyanGlow else ArcCyan
            } else {
                ArcCyan.copy(alpha = 0.35f)
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((28.dp * heightFraction).coerceAtLeast(4.dp))
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                barColor,
                                barColor.copy(alpha = 0.5f)
                            )
                        )
                    )
            )
        }
    }
}
