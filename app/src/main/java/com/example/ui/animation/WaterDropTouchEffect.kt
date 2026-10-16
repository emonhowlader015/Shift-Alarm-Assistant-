package com.example.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlowingMagenta
import com.example.ui.theme.LiquidTeal
import com.example.ui.theme.MidnightCard
import com.example.ui.theme.MidnightCardBorder
import com.example.ui.theme.RoyalViolet
import com.example.ui.theme.VividPurple
import kotlinx.coroutines.launch
import kotlin.math.hypot

data class WaterDropRippleState(
    val center: Offset,
    val animProgress: Animatable<Float, *> = Animatable(0f),
    val alphaAnim: Animatable<Float, *> = Animatable(0.85f)
)

/**
 * Modifier providing interactive RGB water-drop touch animation expanding from tap point.
 */
fun Modifier.waterDropClickable(
    enabled: Boolean = true,
    effectsEnabled: Boolean = true,
    accentStrength: Float = 1.0f,
    onClick: () -> Unit
): Modifier = this.then(
    Modifier.pointerInput(enabled, effectsEnabled) {
        if (!enabled) return@pointerInput
        detectTapGestures(
            onTap = {
                onClick()
            }
        )
    }
)

/**
 * Container component that hosts touch-responsive water drop ripples.
 */
@Composable
fun WaterDropSurface(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    backgroundColor: Color = MidnightCard,
    borderColor: Color = MidnightCardBorder,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val ripples = remember { mutableStateListOf<WaterDropRippleState>() }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onTap = { offset ->
                        if (enabled) {
                            val ripple = WaterDropRippleState(center = offset)
                            ripples.add(ripple)
                            coroutineScope.launch {
                                launch {
                                    ripple.animProgress.animateTo(
                                        targetValue = 1f,
                                        animationSpec = tween(durationMillis = 650, easing = LinearOutSlowInEasing)
                                    )
                                }
                                launch {
                                    ripple.alphaAnim.animateTo(
                                        targetValue = 0f,
                                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                                    )
                                    ripples.remove(ripple)
                                }
                            }
                        }
                        onClick?.invoke()
                    }
                )
            }
    ) {
        content()

        if (enabled && ripples.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val maxDimension = hypot(size.width, size.height)
                ripples.forEach { ripple ->
                    val progress = ripple.animProgress.value
                    val alpha = ripple.alphaAnim.value
                    val currentRadius = progress * (maxDimension * 0.9f).coerceAtLeast(80f)

                    if (alpha > 0.01f && currentRadius > 0f) {
                        // Central droplet flash
                        if (progress < 0.4f) {
                            drawCircle(
                                color = ElectricCyan.copy(alpha = (alpha * 0.5f * (1f - progress / 0.4f))),
                                radius = 24.dp.toPx() * (1f - progress),
                                center = ripple.center
                            )
                        }

                        // Inner expanding droplet wave
                        val waveColors = listOf(
                            ElectricCyan.copy(alpha = alpha * 0.7f),
                            ElectricBlue.copy(alpha = alpha * 0.55f),
                            VividPurple.copy(alpha = alpha * 0.4f),
                            GlowingMagenta.copy(alpha = alpha * 0.25f),
                            Color.Transparent
                        )

                        // Outer glowing circular wave edge
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = waveColors,
                                center = ripple.center,
                                radius = currentRadius.coerceAtLeast(10f)
                            ),
                            radius = currentRadius,
                            center = ripple.center,
                            style = Stroke(
                                width = (12.dp.toPx() * (1f - progress * 0.7f)).coerceAtLeast(2.dp.toPx()),
                                cap = StrokeCap.Round
                            )
                        )

                        // Subtle outer RGB perimeter ring
                        drawCircle(
                            color = LiquidTeal.copy(alpha = alpha * 0.35f),
                            radius = (currentRadius * 0.95f).coerceAtLeast(5f),
                            center = ripple.center,
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

/**
 * Animated liquid RGB gradient border wrapper for hero elements.
 */
@Composable
fun LiquidGlowBorder(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.5.dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "liquid_rgb")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "liquid_angle"
    )

    val gradientBrush = remember(angle) {
        Brush.sweepGradient(
            colors = listOf(
                ElectricCyan,
                ElectricBlue,
                VividPurple,
                GlowingMagenta,
                LiquidTeal,
                ElectricCyan
            )
        )
    }

    Box(
        modifier = modifier
            .border(borderWidth, gradientBrush, shape)
            .clip(shape)
    ) {
        content()
    }
}
