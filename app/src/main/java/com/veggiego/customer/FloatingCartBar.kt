package com.veggiego.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape

@Composable
fun FloatingCartBar(navController: NavController) {
    val itemsCount = CartData.totalItems()
    val totalPrice = CartData.totalPrice()
    AnimatedVisibility(

        visible = itemsCount > 0,

        enter = slideInVertically(

            initialOffsetY = { it / 2 }

        ),

        exit = slideOutVertically(

            targetOffsetY = { it }
        )
    ) {

        Card(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                )
                .clickable {

                    navController.navigate(
                        "cart"
                    )
                },

            shape =
                RoundedCornerShape(24.dp),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 14.dp
                ),

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color.Transparent
                )
        ) {

            Box(

                modifier = Modifier

                    .background(

                        Brush.horizontalGradient(

                            listOf(

                                Color(0xFF16A34A),

                                Color(0xFF22C55E)
                            )
                        )
                    )
                    .padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    )
            ) {

                Row(

                    modifier = Modifier
                        .fillMaxWidth(),

                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.SpaceBetween
                ) {

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(

                            modifier = Modifier

                                .size(46.dp)

                                .background(

                                    Color.White.copy(
                                        alpha = 0.18f
                                    ),

                                    CircleShape
                                ),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Text(

                                text = "🛒",

                                fontSize = 22.sp
                            )
                        }

                        Spacer(
                            modifier =
                                Modifier.width(14.dp)
                        )

                        Column {

                            Text(

                                text =
                                    "$itemsCount item${if (itemsCount > 1) "s" else ""} added",

                                color = Color.White,

                                fontSize = 15.sp,

                                fontWeight =
                                    FontWeight.Bold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(2.dp)
                            )

                            Text(

                                text =
                                    "₹$totalPrice plus taxes",

                                color =
                                    Color.White.copy(
                                        alpha = 0.92f
                                    ),

                                fontSize = 13.sp
                            )
                        }
                    }

                    Row(

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Text(

                            text =
                                "View Cart",

                            color = Color.White,

                            fontWeight =
                                FontWeight.Bold,

                            fontSize = 16.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Icon(

                            imageVector =
                                Icons.Default.ArrowForward,

                            contentDescription = null,

                            tint = Color.White,

                            modifier =
                                Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
