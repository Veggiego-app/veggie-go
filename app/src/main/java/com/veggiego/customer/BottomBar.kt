package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.outlined.Person
import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun BottomBar(

    navController: NavController

) {

    Surface(

        modifier = Modifier.navigationBarsPadding(),

        tonalElevation = 10.dp,

        shadowElevation = 10.dp,

        color = Color.White

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 10.dp
                ),

            horizontalArrangement =
                Arrangement.SpaceEvenly,

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            // ✅ HOME

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier = Modifier
                    .clickable {

                        navController.navigate(
                            "home"
                        )
                    }

            ) {

                Surface(

                    shape =
                        RoundedCornerShape(20.dp),

                    color =
                        Color(0xFFF3E8FF)

                ) {

                    Icon(

                        imageVector =
                            Icons.Filled.Home,

                        contentDescription = null,

                        tint =
                            Color(0xFF4F46E5),

                        modifier = Modifier
                            .padding(
                                horizontal = 18.dp,
                                vertical = 10.dp
                            )
                    )
                }

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text = "Home",

                    fontSize = 12.sp,

                    fontWeight = FontWeight.SemiBold,

                    color = Color(0xFF4F46E5)
                )
            }

            // ✅ CART

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier = Modifier
                    .clickable {

                        navController.navigate(
                            "cart"
                        )
                    }

            ) {

                Icon(

                    imageVector =
                        Icons.Outlined.ShoppingCart,

                    contentDescription = null,

                    tint = Color.Gray,

                    modifier = Modifier.size(26.dp)
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text = "Cart",

                    fontSize = 12.sp,

                    color = Color.Gray
                )
            }

            // ✅ ORDERS

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier = Modifier
                    .clickable {

                        navController.navigate(
                            "orders"
                        )
                    }

            ) {

                Icon(

                    imageVector =
                        Icons.Outlined.ReceiptLong,

                    contentDescription = null,

                    tint = Color.Gray,

                    modifier = Modifier.size(26.dp)
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text = "Orders",

                    fontSize = 12.sp,

                    color = Color.Gray
                )
            }
            // ✅ PROFILE

            Column(

                horizontalAlignment =
                    Alignment.CenterHorizontally,

                modifier = Modifier
                    .clickable {

                        navController.navigate(
                            "profile"
                        )

                    }

            ) {

                Icon(

                    imageVector =
                        Icons.Outlined.Person,

                    contentDescription = null,

                    tint = Color.Gray,

                    modifier = Modifier.size(26.dp)

                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text = "Profile",

                    fontSize = 12.sp,

                    color = Color.Gray

                )

            }
        }
    }
}