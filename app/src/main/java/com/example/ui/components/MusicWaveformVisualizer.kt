package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JoeyCyanGlow
import com.example.ui.theme.JoeyIndigoPrimary

@Composable
fun MusicWaveformVisualizer(
    amplitudes: List<Float>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "idle_wave")
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val barCount = amplitudes.size.coerceAtLeast(16)
            val totalSpacing = (barCount - 1) * 6.dp.toPx()
            val availableWidth = size.width - totalSpacing
            val barWidth = (availableWidth / barCount).coerceAtLeast(4.dp.toPx())
            val maxHeight = size.height * 0.85f

            for (i in 0 until barCount) {
                val amp = if (isPlaying) {
                    amplitudes.getOrElse(i) { 0.1f }
                } else {
                    idlePulse * (0.6f + 0.4f * kotlin.math.sin(i * 0.5f).toFloat())
                }

                val barHeight = (maxHeight * amp).coerceIn(6.dp.toPx(), maxHeight)
                val xOffset = i * (barWidth + 6.dp.toPx())
                val yOffset = (size.height - barHeight) / 2f

                val barBrush = Brush.verticalGradient(
                    colors = listOf(
                        JoeyCyanGlow,
                        Color(0xFF818CF8),
                        JoeyIndigoPrimary
                    ),
                    startY = yOffset,
                    endY = yOffset + barHeight
                )

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(xOffset, yOffset),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }
        }
    }
}
