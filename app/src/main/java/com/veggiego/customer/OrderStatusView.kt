package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderStatusView(status: String) {

    val preparingColor =
        if (status == "Preparing" ||
            status == "Ready" ||
            status == "Out for Delivery" ||
            status == "Delivered"
        ) Color(0xFF2E7D32)
        else Color.LightGray

    val readyColor =
        if (status == "Ready" ||
            status == "Out for Delivery" ||
            status == "Delivered"
        ) Color(0xFF2E7D32)
        else Color.LightGray

    val outColor =
        if (status == "Out for Delivery" ||
            status == "Delivered"
        ) Color(0xFF2E7D32)
        else Color.LightGray

    val deliveredColor =
        if (status == "Delivered")
            Color(0xFF2E7D32)
        else Color.LightGray

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        StatusItem("Preparing", preparingColor)

        StatusItem("Ready", readyColor)

        StatusItem("Out", outColor)

        StatusItem("Done", deliveredColor)
    }
}

@Composable
fun StatusItem(
    text: String,
    color: Color
) {
    val infiniteTransition =

        rememberInfiniteTransition(
            label = ""
        )

    val scale by infiniteTransition.animateFloat(

        initialValue = 1f,

        targetValue =

            if (
                color != Color.LightGray
            ) 1.15f
            else 1f,

        animationSpec =

            infiniteRepeatable(

                animation = tween(
                    900
                ),

                repeatMode =
                    RepeatMode.Reverse
            ),

        label = ""
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(

                    if (
                        color != Color.LightGray
                    )

                        (22 * scale).dp

                    else

                        22.dp
                )
                .background(color, CircleShape)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}