package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ai.JoeyPersona
import com.example.ui.theme.NeonHotPink
import com.example.ui.theme.NeonLaserCyan
import com.example.ui.theme.NeonMagentaPink

@Composable
fun TypingIndicator(
    persona: JoeyPersona,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "typing_dots")

    val dot1Scale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )

    val dot2Scale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 160, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )

    val dot3Scale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, delayMillis = 320, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    val glowAlpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing_glow"
    )

    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        JoeyAvatar(
            size = 36.dp,
            isThinking = true,
            modifier = Modifier.padding(end = 10.dp)
        )

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xFF1B0733),
            modifier = Modifier.border(
                width = 1.dp,
                color = NeonLaserCyan.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(18.dp)
            ),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${persona.displayName} is cooking a response",
                    style = MaterialTheme.typography.labelMedium,
                    color = NeonLaserCyan
                )
                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dot1Scale)
                        .clip(CircleShape)
                        .background(NeonLaserCyan)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dot2Scale)
                        .clip(CircleShape)
                        .background(NeonMagentaPink)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dot3Scale)
                        .clip(CircleShape)
                        .background(NeonHotPink)
                )
            }
        }
    }
}
