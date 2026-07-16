package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ReviewScreen(

    navController: NavController,
    orderId: String

) {

    var review by remember {
        mutableStateOf("")
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

            text = "Write Review",

            style =
                MaterialTheme.typography.headlineMedium
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        OutlinedTextField(

            value = review,

            onValueChange = {
                review = it
            },

            label = {
                Text("Your Review")
            },

            modifier =
                Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Button(

            onClick = {

                FirebaseFirestore
                    .getInstance()
                    .collection("reviews")
                    .document(orderId)
                    .set(

                        mapOf(

                            "orderId" to orderId,
                            "review" to review
                        )
                    )

                navController.popBackStack()
            }

        ) {

            Text(
                "Submit Review"
            )
        }
    }
}