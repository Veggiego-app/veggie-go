package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun RatingScreen(

    navController: NavController,
    orderId: String

) {

    var rating by remember {
        mutableIntStateOf(0)
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally,

        verticalArrangement =
            Arrangement.Center

    ) {

        Text(
            text = "Rate Order",

            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Row {

            for (i in 1..5) {

                IconButton(

                    onClick = {
                        rating = i
                    }

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.Star,

                        contentDescription = null,

                        tint =
                            if (i <= rating)
                                Color(0xFFFFC107)
                            else
                                Color.LightGray
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(

            onClick = {

                FirebaseFirestore
                    .getInstance()
                    .collection("ratings")
                    .document(orderId)
                    .set(

                        mapOf(

                            "orderId" to orderId,
                            "rating" to rating
                        )
                    )

                navController.popBackStack()
            }

        ) {

            Text(
                "Submit Rating"
            )
        }
    }
}