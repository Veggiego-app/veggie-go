package com.veggiego.customer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun DeliveredScreen(

    navController: NavController,

    orderId: String

) {

    val context = LocalContext.current

    var reviewText by remember {
        mutableStateOf("")
    }

    var rating by remember {
        mutableIntStateOf(0)
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),

        horizontalAlignment = Alignment.CenterHorizontally,

        verticalArrangement = Arrangement.Center

    ) {

        Text(
            text = "🎉",
            fontSize = 70.sp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Text(

            text = "Order Delivered Successfully",

            fontSize = 28.sp,

            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Text(

            text = "Hope you enjoyed your food 😎",

            color = Color.Gray
        )

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        Text(

            text = "Rate Your Experience",

            fontWeight = FontWeight.Bold,

            fontSize = 22.sp
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Row {

            repeat(5) { index ->

                IconButton(

                    onClick = {

                        rating = index + 1
                    }

                ) {

                    Icon(

                        imageVector = Icons.Default.Star,

                        contentDescription = null,

                        tint =

                            if (index < rating)

                                Color(0xFFFFC107)

                            else

                                Color.LightGray,

                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        OutlinedTextField(

            value = reviewText,

            onValueChange = {

                reviewText = it
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),

            placeholder = {

                Text(
                    "Write your review..."
                )
            },

            shape = RoundedCornerShape(20.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        // ✅ SUBMIT REVIEW BUTTON

        Button(

            onClick = {

                if (rating == 0) {

                    Toast.makeText(
                        context,
                        "Please give rating",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@Button
                }

                val db = FirebaseFirestore.getInstance()

                val reviewData = Review(

                    restaurantId = "veg_pizza_hub",

                    restaurantName = "Veg Pizza Hub",

                    customerName = "Customer",

                    rating = rating,

                    review = reviewText,

                    timestamp = System.currentTimeMillis()

                )

                db.collection("reviews")
                    .add(reviewData)

                    .addOnSuccessListener {
                        db.collection("orders")
                            .document(orderId)
                            .update(
                                "isReviewed",
                                true
                            )
                        Toast.makeText(
                            context,
                            "Review Submitted 😍",
                            Toast.LENGTH_SHORT
                        ).show()

                        navController.navigate("home")

                    }

                    .addOnFailureListener {

                        Toast.makeText(
                            context,
                            "Failed: ${it.message}",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

            },

            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),

            shape = RoundedCornerShape(18.dp),

            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFC107)
            )

        ) {

            Text(

                text = "Submit Review",

                color = Color.Black,

                fontSize = 18.sp
            )
        }

        Spacer(
            modifier = Modifier.height(16.dp)
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

            shape = RoundedCornerShape(18.dp)

        ) {

            Text(

                text = "Go Home",

                fontSize = 18.sp
            )
        }
    }
}