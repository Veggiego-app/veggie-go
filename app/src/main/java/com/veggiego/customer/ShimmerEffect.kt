package com.veggiego.customer

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(): Brush {

    val transition =
        rememberInfiniteTransition(
            label = ""
        )

    val translateAnim =
        transition.animateFloat(

            initialValue = 0f,

            targetValue = 1200f,

            animationSpec =
                infiniteRepeatable(

                    animation = tween(
                        durationMillis = 1100
                    ),

                    repeatMode =
                        RepeatMode.Restart
                ),

            label = ""
        )

    return Brush.linearGradient(

        colors = listOf(

            Color.LightGray.copy(
                alpha = 0.7f
            ),

            Color.LightGray.copy(
                alpha = 0.2f
            ),

            Color.LightGray.copy(
                alpha = 0.7f
            )
        ),

        start = Offset.Zero,

        end = Offset(
            x = translateAnim.value,
            y = translateAnim.value
        )
    )
}

@Composable
fun ShimmerMenuCard() {

    val brush =
        shimmerBrush()

    Column(

        modifier = Modifier
            .padding(14.dp)

    ) {

        Box(

            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(

                    brush,

                    RoundedCornerShape(24.dp)
                )
        )

        Spacer(
            modifier = Modifier
                .height(12.dp)
        )

        Box(

            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(22.dp)
                .background(

                    brush,

                    RoundedCornerShape(12.dp)
                )
        )

        Spacer(
            modifier = Modifier
                .height(8.dp)
        )

        Box(

            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(18.dp)
                .background(

                    brush,

                    RoundedCornerShape(12.dp)
                )
        )
    }
}