package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material3.Card

@Composable
fun FavoritesScreen(

    navController: NavController

) {

    val db =
        FirebaseFirestore.getInstance()

    val auth =
        FirebaseAuth.getInstance()

    var favorites by remember {

        mutableStateOf(
            listOf<FavoriteRestaurant>()
        )
    }

    var loading by remember {

        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        val uid =
            auth.currentUser?.uid

        if (uid == null) {

            loading = false
            return@LaunchedEffect
        }

        db.collection("favorites")
            .document(uid)
            .collection("restaurants")
            .get()

            .addOnSuccessListener { result ->

                favorites =

                    result.documents.mapNotNull {

                        it.toObject(
                            FavoriteRestaurant::class.java
                        )
                    }

                loading = false
            }

            .addOnFailureListener {

                loading = false
            }
    }

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)

    ) {

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        Text(

            text = "❤️ Favorites",

            fontSize = 30.sp,

            fontWeight = FontWeight.Bold,

            modifier =
                Modifier.padding(
                    horizontal = 20.dp
                )
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        when {

            loading -> {

                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center

                ) {

                    CircularProgressIndicator()
                }
            }

            favorites.isEmpty() -> {

                Box(

                    modifier =
                        Modifier.fillMaxSize(),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Column(

                        horizontalAlignment =
                            Alignment.CenterHorizontally

                    ) {

                        Text(
                            text = "💔",
                            fontSize = 60.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(12.dp)
                        )

                        Text(

                            text =
                                "No favorites yet",

                            fontSize = 22.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(6.dp)
                        )

                        Text(

                            text =
                                "Restaurants you like\nwill appear here",

                            color = Color.Gray,

                            fontSize = 16.sp
                        )
                    }
                }
            }

            else -> {

                LazyColumn {

                    items(favorites) { restaurant ->

                        Card(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = 16.dp,
                                    vertical = 8.dp
                                ),

                            onClick = {

                                navController.navigate(

                                    "restaurant_detail/${restaurant.id}"
                                )
                            }

                        ) {

                            Row(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),

                                verticalAlignment =
                                    Alignment.CenterVertically

                            ) {

                                AsyncImage(

                                    model =
                                        restaurant.imageUrl,

                                    contentDescription = null,

                                    modifier = Modifier
                                        .size(90.dp),

                                    contentScale =
                                        ContentScale.Crop
                                )

                                Spacer(
                                    modifier = Modifier.width(14.dp)
                                )

                                Column {

                                    Text(

                                        text =
                                            restaurant.restaurantName,

                                        fontSize = 20.sp,

                                        fontWeight =
                                            FontWeight.Bold
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(6.dp)
                                    )

                                    Text(

                                        text =
                                            "❤️ Favorite Restaurant",

                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(100.dp)
                        )
                    }
                }
            }
        }
    }
}