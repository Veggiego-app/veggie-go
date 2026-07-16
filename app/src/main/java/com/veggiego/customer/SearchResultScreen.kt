package com.veggiego.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

data class SearchFoodResult(

    val restaurantId: String = "",

    val restaurantName: String = "",

    val foodName: String = "",

    val imageUrl: String = "",

    val rating: String = "",

    val deliveryTime: String = ""
)

@Composable
fun SearchResultScreen(

    navController: NavController,

    query: String

) {

    var foodResults by remember {

        mutableStateOf<List<SearchFoodResult>>(
            emptyList()
        )
    }

    LaunchedEffect(query) {

        FirebaseFirestore
            .getInstance()
            .collection("restaurants")
            .get()
            .addOnSuccessListener { restaurantResult ->

                val tempList =
                    mutableListOf<SearchFoodResult>()

                restaurantResult.documents.forEach { restaurantDoc ->

                    val restaurantName =
                        restaurantDoc.getString(
                            "name"
                        ) ?: ""

                    val restaurantId =
                        restaurantDoc.id

                    val rating =
                        restaurantDoc.get(
                            "rating"
                        )?.toString() ?: "4.5"

                    val deliveryTime =
                        restaurantDoc.getString(
                            "deliveryTime"
                        ) ?: "20-30 min"

                    // ✅ RESTAURANT NAME SEARCH

                    if (

                        restaurantName.contains(
                            query,
                            ignoreCase = true
                        )

                    ) {

                        tempList.add(

                            SearchFoodResult(

                                restaurantId =
                                    restaurantId,

                                restaurantName =
                                    restaurantName,

                                foodName =
                                    "Open Restaurant",

                                rating =
                                    rating,

                                deliveryTime =
                                    deliveryTime
                            )
                        )
                    }

                    // ✅ FOOD ITEM SEARCH

                    FirebaseFirestore
                        .getInstance()
                        .collection("restaurants")
                        .document(restaurantId)
                        .collection("menu")
                        .get()
                        .addOnSuccessListener { menuResult ->

                            menuResult.documents.forEach { menuDoc ->

                                val visible =
                                    menuDoc.getBoolean("visible") ?: true

                                if (!visible) {
                                    return@forEach
                                }

                                val foodName =
                                    menuDoc.getString(
                                        "name"
                                    ) ?: ""

                                if (

                                    foodName.contains(
                                        query,
                                        ignoreCase = true
                                    )

                                ) {

                                    tempList.add(

                                        SearchFoodResult(

                                            restaurantId =
                                                restaurantId,

                                            restaurantName =
                                                restaurantName,

                                            foodName =
                                                foodName,

                                            imageUrl =
                                                menuDoc.getString(
                                                    "image"
                                                ) ?: "",

                                            rating =
                                                rating,

                                            deliveryTime =
                                                deliveryTime
                                        )
                                    )

                                    foodResults =
                                        tempList.distinctBy {

                                            it.restaurantId +
                                                    it.foodName
                                        }
                                }
                            }

                            foodResults =
                                tempList.distinctBy {

                                    it.restaurantId +
                                            it.foodName
                                }
                        }
                }
            }
    }

    LazyColumn(

        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)

    ) {

        items(foodResults) { result ->

            Card(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable {

                        if (

                            result.foodName ==
                            "Open Restaurant"

                        ) {

                            navController.navigate(

                                "restaurant_detail/" +
                                        result.restaurantId
                            )

                        } else {

                            navController.navigate(

                                "restaurant_detail/" +
                                        result.restaurantId +
                                        "/" +
                                        result.foodName
                            )
                        }
                    }

            ) {

                Column(

                    modifier = Modifier
                        .padding(16.dp)

                ) {

                    Text(

                        text = result.foodName,

                        style =
                            MaterialTheme
                                .typography
                                .titleLarge
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            result.restaurantName
                    )

                    Spacer(
                        modifier =
                            Modifier.height(6.dp)
                    )

                    Text(
                        text =
                            "⭐ ${result.rating}"
                    )

                    Text(
                        text =
                            result.deliveryTime
                    )
                }
            }
        }
    }
}