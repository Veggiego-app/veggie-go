package com.veggiego.customer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantMenuScreen(

    navController: NavController,

    restaurantName: String

) {

    var menuItems by remember {

        mutableStateOf<List<MenuItem>>(
            emptyList()
        )
    }

    var loading by remember {

        mutableStateOf(true)
    }

    var searchText by remember {

        mutableStateOf("")
    }

    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("restaurants")
            .document(restaurantName)
            .collection("menu")
            .addSnapshotListener { value, error ->

                if (error != null) {

                    loading = false
                    return@addSnapshotListener
                }

                if (value != null) {

                    menuItems =
                        value.documents.mapNotNull {

                            it.toObject(
                                MenuItem::class.java
                            )
                        }

                    loading = false
                }
            }
    }

    val filteredItems =

        menuItems.filter {

            it.name.contains(
                searchText,
                ignoreCase = true
            )
        }

    Scaffold(

        floatingActionButton = {

            AnimatedVisibility(

                visible =
                    CartData.items.isNotEmpty(),

                enter =
                    fadeIn() +
                            slideInVertically()

            ) {

                FloatingCartBar(
                    navController
                )
            }
        }

    ) { padding ->

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)

        ) {

            item {

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )

                OutlinedTextField(

                    value = searchText,

                    onValueChange = {

                        searchText = it
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),

                    leadingIcon = {

                        Icon(
                            imageVector =
                                Icons.Default.Search,

                            contentDescription = null
                        )
                    },

                    placeholder = {

                        Text(
                            text = "Search food..."
                        )
                    },

                    singleLine = true
                )

                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )

                OfferBanner()

                Spacer(
                    modifier = Modifier
                        .height(10.dp)
                )

                Text(

                    text = "Recommended 😍",

                    style =
                        MaterialTheme.typography
                            .headlineSmall,

                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                )

                Spacer(
                    modifier = Modifier
                        .height(6.dp)
                )
            }

            if (loading) {

                items(5) {

                    ShimmerMenuCard()
                }

            } else {

                items(filteredItems) { item ->

                    MenuItemCard(

                        item = item,

                        restaurantId = restaurantName,

                        restaurantName = restaurantName
                    )
                }
            }

            item {

                Spacer(
                    modifier = Modifier
                        .height(120.dp)
                )
            }
        }
    }
}