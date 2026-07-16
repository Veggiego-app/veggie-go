package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OfferBanner() {

    Box(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 10.dp
            )
            .height(100.dp)
            .background(

                brush = Brush.linearGradient(

                    colors = listOf(
                        Color(0xFF2C2C2C),
                        Color(0xFF000000)
                    )
                ),

                shape = RoundedCornerShape(28.dp)
            )
    ) {

        Column(

            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(18.dp)

        ) {

            Text(

                text = "🛵 FREE DELIVERY",

                fontSize = 25.sp,

                fontWeight = FontWeight.Bold,

                color = Color.White
            )

            Spacer(
                modifier = Modifier
                    .height(10.dp)
            )

            Text(

                text = "On all orders above ₹199",

                fontSize = 17.sp,

                color = Color.White
            )

            Spacer(
                modifier = Modifier
                    .height(14.dp)
            )
        }
    }
}