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
            Color(0xFFB8B5B5),
            Color(0xFF8F8B8B),
            Color(0xFFB8B5B5)
        ),
        start = Offset(0f, 0f),
        end = Offset(offsetX, 0f)
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