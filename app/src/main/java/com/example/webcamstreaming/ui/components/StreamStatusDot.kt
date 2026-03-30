package com.example.webcamstreaming.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun StreamStatusDot(
    status: StreamStatus,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp,
) {
    val targetColor = when (status) {
        StreamStatus.STREAMING -> Color(0xFF2E7D32) // green
        StreamStatus.BUFFERING -> Color(0xFFFFC107) // yellow
        StreamStatus.ERROR -> Color(0xFFB00020) // red (theme error)
    }

    val color = animateColorAsState(targetColor, label = "statusDotColor").value

    BoxCircle(modifier = modifier, size = size, color = color)
}

@Composable
private fun BoxCircle(
    modifier: Modifier,
    size: Dp,
    color: Color,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
    )
}

