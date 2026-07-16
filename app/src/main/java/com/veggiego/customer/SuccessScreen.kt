package com.veggiego.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun SuccessScreen(

    navController: NavController,

    orderId: String

) {

    val context = LocalContext.current

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center

    ) {

        Text(

            text = "🎉",

            fontSize = 80.sp
        )

        Spacer(
            modifier =
                Modifier.height(24.dp)
        )

        Text(

            text =
                "Order Placed Successfully",

            fontSize = 30.sp,

            fontWeight =
                FontWeight.Bold
        )

        Spacer(
            modifier =
                Modifier.height(12.dp)
        )

        Text(

            text =
                "Your delicious food is being prepared 😎",

            color =
                Color.Gray,

            fontSize = 18.sp
        )

        Spacer(
            modifier =
                Modifier.height(40.dp)
        )

        // ✅ TRACK ORDER BUTTON

        Button(

            onClick = {

                navController.navigate(
                    "tracking/$orderId"
                )
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),

            shape =
                RoundedCornerShape(18.dp)

        ) {

            Text(

                text =
                    "Track Order",

                fontSize = 18.sp
            )
        }

        Spacer(
            modifier =
                Modifier.height(16.dp)
        )

        // ✅ GO HOME BUTTON

        OutlinedButton(

            onClick = {

                navController.navigate("home") {

                    popUpTo(0)
                }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),

            shape =
                RoundedCornerShape(18.dp)

        ) {

            Text(

                text =
                    "Go Home",

                fontSize = 18.sp
            )
        }
    }
}