package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.NeonHotPink
import com.example.ui.theme.NeonLaserCyan
import com.example.ui.theme.NeonMagentaPink
import com.example.ui.theme.NeonVibrantPurple

@Composable
fun JoeyAvatar(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    emoji: String = "😎", // Kept for backwards compatibility but unused
    isThinking: Boolean = false,
    isSpeaking: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isThinking || isSpeaking) 1.12f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_scale"
    )

    // Switch image based on active state (thinking or speaking)
    val isJoeyActive = isThinking || isSpeaking
    val currentImageRes = if (isJoeyActive) R.drawable.joey_avatar else R.drawable.joey_cap_logo

    Box(
        modifier = modifier
            .size(size)
            .scale(if (isJoeyActive) pulseScale else 1f),
        contentAlignment = Alignment.Center
    ) {
        // Outer 80s Neon Laser Glow ring
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            NeonLaserCyan.copy(alpha = if (isJoeyActive) 1f else 0.5f),
                            NeonMagentaPink.copy(alpha = if (isJoeyActive) 1f else 0.5f),
                            NeonVibrantPurple.copy(alpha = if (isJoeyActive) 1f else 0.5f),
                            NeonHotPink.copy(alpha = if (isJoeyActive) 1f else 0.5f),
                            NeonLaserCyan.copy(alpha = if (isJoeyActive) 1f else 0.5f)
                        )
                    )
                )
        )

        // Inner Avatar Image
        Box(
            modifier = Modifier
                .size(size - 3.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F021B))
                .border(width = 1.5.dp, color = NeonLaserCyan, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = currentImageRes),
                contentDescription = "Joey AI Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .scale(scaleX = if (isJoeyActive) -1f else 1f, scaleY = 1f) // Flip face horizontally when active
            )
        }
    }
}
