package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

data class FoodRestaurantModel(

    val restaurantId: String = "",

    val restaurantName: String = "",

    val imageUrl: String = "",

    val rating: String = "",

    val deliveryTime: String = "",

    val offer: String = "",

    val isPureVeg: Boolean = true,

    val online: Boolean = true,

    val temporaryClosed: Boolean = false,

    val openingText: String = ""
)

@Composable
fun FoodRestaurantScreen(

    navController: NavController,

    foodName: String
) {

    val db = FirebaseFirestore.getInstance()

    var restaurants by remember {

        mutableStateOf(
            listOf<FoodRestaurantModel>()
        )
    }

    LaunchedEffect(foodName) {

        db.collection("restaurants")

            .get()

            .addOnSuccessListener { restaurantSnapshot ->

                val result =
                    mutableListOf<FoodRestaurantModel>()

                var completed = 0

                restaurantSnapshot.documents.forEach { restaurant ->

                    db.collection("restaurants")

                        .document(restaurant.id)

                        .collection("menu")

                        .get()

                        .addOnSuccessListener { menuSnapshot ->

                            val found =

                                menuSnapshot.documents.any {

                                    val itemName =
                                        it.getString("name") ?: ""

                                    itemName.equals(
                                        foodName,
                                        ignoreCase = true
                                    )
                                }

                            if (found) {

                                result.add(

                                    FoodRestaurantModel(

                                        restaurantId = restaurant.id,

                                        restaurantName =
                                            restaurant.getString("name") ?: "",

                                        imageUrl =
                                            restaurant.getString("logoUrl")
                                                ?: restaurant.getString("imageUrl")
                                                ?: "",

                                        rating =
                                            restaurant.get("rating")
                                                ?.toString()
                                                ?: "4.5",

                                        deliveryTime =
                                            restaurant.getString("deliveryTime")
                                                ?: "20-30 min",

                                        offer =
                                            restaurant.getString("offer")
                                                ?: "",

                                        isPureVeg =
                                            restaurant.getBoolean("isPureVeg")
                                                ?: true,

                                        online =
                                            restaurant.getBoolean("online")
                                                ?: true,

                                        temporaryClosed =
                                            restaurant.getBoolean("temporaryClosed")
                                                ?: false,

                                        openingText =
                                            restaurant.getString("openingText")
                                                ?: ""

                                    )

                                )
                            }

                            completed++

                            if (completed == restaurantSnapshot.size()) {

                                restaurants =

                                    result
                                        .distinctBy {

                                            it.restaurantId
                                        }
                                        .sortedWith(
                                            compareByDescending<FoodRestaurantModel> { it.online }
                                                .thenBy { it.temporaryClosed }
                                                .thenBy { it.restaurantName }
                                        )

                            }

                        }

                }

            }

    }

    Box(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)

    ) {

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)

        ) {

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                IconButton(

                    onClick = {

                        navController.popBackStack()
                    }

                ) {

                    Icon(

                        imageVector =
                            Icons.Default.ArrowBack,

                        contentDescription = null,

                        tint = Color.Red
                    )
                }

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Column {

                    Text(

                        text = foodName,

                        fontWeight = FontWeight.Bold,

                        fontSize = 26.sp
                    )

                    Text(

                        text =
                            "Restaurants serving this item 😎",

                        color = Color.Gray
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            LazyColumn(

                modifier = Modifier.weight(1f)

            ) {

                items(restaurants) { restaurant ->

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .clickable {

                                navController.navigate(

                                    "restaurant_detail/" +
                                            "${restaurant.restaurantId}/" +
                                            foodName
                                )
                            },

                        shape =
                            RoundedCornerShape(20.dp),

                        elevation =
                            CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            )
                    ) {

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)

                        ) {

                            AsyncImage(

                                model = restaurant.imageUrl,

                                contentDescription = null,

                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            18.dp
                                        )
                                    ),

                                contentScale =
                                    ContentScale.Crop
                            )

                            Spacer(
                                modifier = Modifier.width(14.dp)
                            )

                            Column(

                                modifier = Modifier.weight(1f)

                            ) {

                                Text(

                                    text =
                                        restaurant.restaurantName,

                                    fontWeight = FontWeight.Bold,

                                    fontSize = 22.sp
                                )

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Row(

                                    verticalAlignment =
                                        Alignment.CenterVertically

                                ) {

                                    Icon(

                                        imageVector =
                                            Icons.Default.Star,

                                        contentDescription = null,

                                        tint = Color(0xFF2E7D32)
                                    )

                                    Spacer(
                                        modifier = Modifier.width(4.dp)
                                    )

                                    Text(
                                        text =
                                            restaurant.rating
                                    )

                                    Spacer(
                                        modifier = Modifier.width(12.dp)
                                    )

                                    Text(
                                        text =
                                            restaurant.deliveryTime
                                    )
                                }

                                Spacer(
                                    modifier = Modifier.height(8.dp)
                                )

                                Text(

                                    text =
                                        "Contains \"$foodName\"",

                                    color = Color(0xFFE53935),

                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                item {

                    Spacer(
                        modifier =
                            Modifier.height(120.dp)
                    )
                }
            }
        }

        Box(

            modifier = Modifier
                .align(
                    Alignment.BottomCenter
                )

        ) {

            FloatingCartBar(
                navController
            )
        }
    }
}