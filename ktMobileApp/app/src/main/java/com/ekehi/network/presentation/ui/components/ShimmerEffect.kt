package com.ekehi.network.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Brand colors for shimmer effect - matching app theme
private val ShimmerBaseColor = Color(0x33FFA000) // 20% opacity gold
private val ShimmerHighlightColor = Color(0x66FFA000) // 40% opacity gold
private val ShimmerDarkColor = Color(0x1AFFA000) // 10% opacity gold

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    targetValue: Float = 1000f,
    animationDelay: Int = 500
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -targetValue,
        targetValue = targetValue,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            ShimmerDarkColor,
            ShimmerBaseColor,
            ShimmerHighlightColor,
            ShimmerBaseColor,
            ShimmerDarkColor
        ),
        start = Offset(0f, 0f),
        end = Offset(offsetX, offsetX / 2)
    )

    Box(
        modifier = modifier.background(shimmerBrush)
    )
}

@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
    ) {
        ShimmerEffect(
            modifier = Modifier.matchParentSize()
        )
        content()
    }
}