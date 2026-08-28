package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.NeonHotPink
import com.example.ui.theme.NeonLaserCyan
import com.example.ui.theme.NeonMagentaPink
import com.example.ui.theme.NeonVibrantPurple
import com.example.ui.theme.NeonYellowGlow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated 80s Retro Neon Grid Background Composable.
 * Uses Canvas rendering with animated perspective synthwave grid lines,
 * dynamic horizon glow, crossing cyber laser rays, and vibrant purple & cyan illumination.
 * Includes a startup animation with a laser explosion and persistent Joey avatar.
 */
@Composable
fun RetroGridBackground(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "synthwave_grid_anim")

    // Startup Animation State
    var isStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isStarted = true
    }

    val startupProgress by animateFloatAsState(
        targetValue = if (isStarted) 1f else 0f,
        animationSpec = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
        label = "startup_anim"
    )

    // Forward motion animation for the perspective horizontal lines
    val gridScrollProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_scroll"
    )

    // Breathing neon pulse for cyan & purple laser glows
    val neonPulse by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_pulse"
    )

    // Laser beam sweeping effect
    val laserSweep by infiniteTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "laser_sweep"
    )

    // Pre-calculated star particle offsets for background depth
    val particles = remember {
        List(24) { i ->
            Triple(
                (i * 37 % 100) / 100f,
                (i * 53 % 60) / 100f,
                (i % 3 + 1.5f)
            )
        }
    }

    // Pre-calculated explosion lines for opening animation
    val explosionLines = remember {
        List(30) {
            val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
            val speed = Random.nextFloat() * 0.5f + 0.5f
            val length = Random.nextFloat() * 100f + 50f
            val isCyan = Random.nextBoolean()
            ExplosionLine(angle, speed, length, isCyan)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base Canvas for Grid and Background
        Canvas(modifier = Modifier.fillMaxSize().alpha(startupProgress.coerceIn(0f, 1f))) {
            val width = size.width
            val height = size.height

            // 1. Deep Space 80s Gradient (Purple/Black Cosmic Void)
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070114), // Deep cosmic black-violet
                        Color(0xFF140529), // Rich midnight synthwave purple
                        Color(0xFF280746), // Vibrant synth purple
                        Color(0xFF130424)  // Dark cyber floor
                    ),
                    startY = 0f,
                    endY = height
                )
            )

            // 2. Ambient Cyber Star Dust in Upper Sky
            particles.forEachIndexed { index, (relX, relY, radius) ->
                val particleAlpha = (0.25f + 0.35f * sin((index + neonPulse * 6.28f).toDouble()).toFloat()).coerceIn(0.1f, 0.7f)
                val color = if (index % 2 == 0) NeonLaserCyan else NeonHotPink
                drawCircle(
                    color = color.copy(alpha = particleAlpha),
                    radius = radius,
                    center = Offset(relX * width, relY * height)
                )
            }

            // 3. Crossing Diagonal Laser Beams (80s Cyber Neon aesthetic)
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonLaserCyan.copy(alpha = 0f),
                        NeonLaserCyan.copy(alpha = neonPulse * 0.35f),
                        NeonLaserCyan.copy(alpha = 0f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(width, height * 0.75f)
                ),
                start = Offset(-40f, height * 0.05f),
                end = Offset(width + 40f, height * 0.72f),
                strokeWidth = 2.5f
            )

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        NeonHotPink.copy(alpha = 0f),
                        NeonHotPink.copy(alpha = neonPulse * 0.4f),
                        NeonHotPink.copy(alpha = 0f)
                    ),
                    start = Offset(width, 0f),
                    end = Offset(0f, height * 0.65f)
                ),
                start = Offset(width + 40f, height * 0.08f),
                end = Offset(-40f, height * 0.68f),
                strokeWidth = 2.5f
            )

            // Sweeping Holographic Laser Streak
            val sweepX = laserSweep * width
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonLaserCyan.copy(alpha = 0.5f),
                        NeonVibrantPurple.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = Offset(sweepX, height * 0.3f),
                    radius = width * 0.4f
                ),
                start = Offset(sweepX - 80f, height * 0.15f),
                end = Offset(sweepX + 80f, height * 0.45f),
                strokeWidth = 3f
            )

            // 4. Perspective 80s Synthwave Grid
            val horizonY = height * 0.58f
            val gridDepth = height - horizonY
            val vanishingPointX = width / 2f

            // Horizon Neon Glow Halo
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        NeonVibrantPurple.copy(alpha = 0.22f * neonPulse),
                        NeonLaserCyan.copy(alpha = 0.3f * neonPulse),
                        NeonHotPink.copy(alpha = 0.25f * neonPulse),
                        Color.Transparent
                    ),
                    startY = horizonY - 60f,
                    endY = horizonY + 50f
                ),
                topLeft = Offset(0f, horizonY - 60f),
                size = androidx.compose.ui.geometry.Size(width, 110f)
            )

            // Sharp Horizon Laser Line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        NeonLaserCyan.copy(alpha = 0.1f),
                        NeonHotPink.copy(alpha = 0.85f),
                        NeonLaserCyan.copy(alpha = 1.0f),
                        NeonHotPink.copy(alpha = 0.85f),
                        NeonLaserCyan.copy(alpha = 0.1f)
                    )
                ),
                start = Offset(0f, horizonY),
                end = Offset(width, horizonY),
                strokeWidth = 2.8f
            )

            // Vanishing Perspective Radial Lines (Vertical Grid Rails)
            val numVerticalRays = 18
            for (i in -numVerticalRays / 2..numVerticalRays / 2) {
                val bottomSpacing = width / 5.2f
                val startTopX = vanishingPointX + (i * 9f)
                val endBottomX = vanishingPointX + (i * bottomSpacing)

                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            NeonLaserCyan.copy(alpha = 0.08f),
                            NeonLaserCyan.copy(alpha = 0.28f + (neonPulse * 0.12f))
                        ),
                        startY = horizonY,
                        endY = height
                    ),
                    start = Offset(startTopX, horizonY),
                    end = Offset(endBottomX, height),
                    strokeWidth = 1.4f
                )
            }

            // Perspective Horizontal Lines
            val horizontalSegments = 15
            for (i in 1..horizontalSegments) {
                val baseNormalized = (i.toFloat() + gridScrollProgress) / horizontalSegments
                if (baseNormalized in 0f..1.1f) {
                    val depthCurve = baseNormalized * baseNormalized
                    val lineY = horizonY + (depthCurve * gridDepth)

                    if (lineY in horizonY..height) {
                        val lineAlpha = (depthCurve * 0.45f + 0.05f).coerceIn(0.08f, 0.48f)
                        val strokeWidth = 1.0f + (depthCurve * 1.8f)

                        drawLine(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    NeonHotPink.copy(alpha = lineAlpha * 0.3f),
                                    NeonHotPink.copy(alpha = lineAlpha),
                                    NeonLaserCyan.copy(alpha = lineAlpha * 1.1f),
                                    NeonHotPink.copy(alpha = lineAlpha),
                                    NeonHotPink.copy(alpha = lineAlpha * 0.3f)
                                )
                            ),
                            start = Offset(0f, lineY),
                            end = Offset(width, lineY),
                            strokeWidth = strokeWidth
                        )
                    }
                }
            }
        }

        // Opening Animation Laser Explosion Layer
        if (startupProgress > 0.1f && startupProgress < 0.8f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                // Explosion progress goes from 0 to 1 over the 0.1->0.8 window
                val explosionProgress = ((startupProgress - 0.1f) / 0.7f).coerceIn(0f, 1f)
                
                // Draw outward expanding lines
                val currentRadius = explosionProgress * size.width * 1.5f
                val fadeOut = 1f - explosionProgress
                
                explosionLines.forEach { line ->
                    val lineDistance = currentRadius * line.speed
                    val startX = cx + cos(line.angle) * (lineDistance - line.length)
                    val startY = cy + sin(line.angle) * (lineDistance - line.length)
                    val endX = cx + cos(line.angle) * lineDistance
                    val endY = cy + sin(line.angle) * lineDistance
                    
                    val color = if (line.isCyan) NeonLaserCyan else NeonHotPink
                    
                    drawLine(
                        color = color.copy(alpha = fadeOut * 0.8f),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        // Persistent Background Avatar (Starts large and prominent, settles into background)
        val avatarAlpha = when {
            startupProgress < 0.4f -> 0f // hidden initially
            startupProgress < 0.7f -> {
                // fade in strongly
                ((startupProgress - 0.4f) / 0.3f)
            }
            else -> {
                // fade out to background opacity (0.15f)
                1f - (((startupProgress - 0.7f) / 0.3f) * 0.85f)
            }
        }
        
        val avatarScale = when {
            startupProgress < 0.4f -> 0.5f
            startupProgress < 0.7f -> {
                // scale up to full
                0.5f + (((startupProgress - 0.4f) / 0.3f) * 0.7f) // reaches 1.2f
            }
            else -> {
                // scale down to normal background size
                1.2f - (((startupProgress - 0.7f) / 0.3f) * 0.2f) // settles at 1.0f
            }
        }

        Image(
            painter = painterResource(id = R.drawable.joey_avatar),
            contentDescription = "Joey Avatar Background",
            modifier = Modifier
                .align(Alignment.Center)
                .size(360.dp)
                .scale(avatarScale)
                .alpha(avatarAlpha)
        )
    }
}

data class ExplosionLine(
    val angle: Float,
    val speed: Float,
    val length: Float,
    val isCyan: Boolean
)
