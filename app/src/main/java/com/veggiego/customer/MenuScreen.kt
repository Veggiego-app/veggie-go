package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(

    navController: NavController,

    restaurantId: String

) {

    var menuItems by remember {

        mutableStateOf(
            listOf<MenuItem>()
        )
    }

    var filteredItems by remember {

        mutableStateOf(
            listOf<MenuItem>()
        )
    }

    var searchText by remember {

        mutableStateOf("")
    }

    var isLoading by remember {

        mutableStateOf(true)
    }

    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()

            .collection("restaurants")

            .document(restaurantId)

            .collection("menu")

            .addSnapshotListener { value, error ->

                if (error != null) {

                    isLoading = false
                    return@addSnapshotListener
                }

                val list =
                    mutableListOf<MenuItem>()

                value?.documents?.forEach { doc ->

                    list.add(

                        MenuItem(

                            name =
                                doc.getString("name") ?: "",

                            variants =

                                (doc.get("variants")
                                        as? List<Map<String, Any>>)

                                    ?.map {

                                        VariantModel(

                                            name =
                                                it["name"]
                                                        as? String ?: "",

                                            price =
                                                (it["price"]
                                                        as? Long)
                                                    ?.toInt() ?: 0
                                        )
                                    }

                                    ?: emptyList(),

                            category =
                                doc.getString("category") ?: "",

                            image =
                                doc.getString("image") ?: "",

                            veg =
                                doc.getBoolean("veg") ?: true,

                            available =
                                doc.getBoolean("available") ?: true,

                            price =

                                (doc.getLong("price")
                                    ?: 0L).toInt(),

                            description =

                                doc.getString("description")
                                    ?: ""
                        )
                    )
                }

                menuItems = list

                filteredItems = list

                isLoading = false
            }
    }

    LaunchedEffect(searchText) {

        filteredItems =

            menuItems.filter {

                it.name.contains(
                    searchText,
                    ignoreCase = true
                )
            }
    }

    Scaffold(

        floatingActionButton = {

            if (CartData.items.isNotEmpty()) {

                FloatingCartBar(
                    navController
                )
            }
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(padding)

        ) {

            OutlinedTextField(

                value = searchText,

                onValueChange = {

                    searchText = it
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

                leadingIcon = {

                    Icon(
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

            OfferBanner()

            Spacer(
                modifier = Modifier.height(10.dp)
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
                modifier = Modifier.height(10.dp)
            )

            if (isLoading) {

                LazyColumn {

                    items(5) {

                        ShimmerMenuCard()
                    }
                }

            } else {

                LazyColumn(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)

                ) {

                    items(filteredItems) { item ->

                        var isFavorite by remember {

                            mutableStateOf(false)
                        }

                        val scope =
                            rememberCoroutineScope()

                        var showVariantSheet by remember {

                            mutableStateOf(false)
                        }

                        val cartItem =

                            CartData.items.find {

                                it.item.name ==
                                        item.name

                                        &&

                                        it.selectedVariant == null
                            }

                        Card(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),

                            shape =
                                RoundedCornerShape(24.dp),

                            elevation =
                                CardDefaults.cardElevation(
                                    defaultElevation = 8.dp
                                )

                        ) {

                            Column {

                                AsyncImage(

                                    model = item.image,

                                    contentDescription = null,

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(190.dp),

                                    contentScale =
                                        ContentScale.Crop
                                )

                                Column(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                                ) {

                                    Row(

                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween,

                                        verticalAlignment =
                                            Alignment.CenterVertically

                                    ) {

                                        Text(

                                            text = item.name,

                                            fontSize = 22.sp,

                                            fontWeight =
                                                FontWeight.Bold
                                        )

                                        IconButton(

                                            onClick = {

                                                scope.launch {

                                                    FavoriteManager
                                                        .toggleFoodFavorite(

                                                            FavoriteFood(

                                                                id = item.name,

                                                                foodName =
                                                                    item.name,

                                                                restaurantName =
                                                                    restaurantId,

                                                                imageUrl =
                                                                    item.image
                                                            )
                                                        )

                                                    isFavorite =
                                                        !isFavorite
                                                }
                                            }
                                        ) {

                                            Icon(

                                                imageVector =

                                                    if (isFavorite)

                                                        Icons.Filled.Favorite

                                                    else

                                                        Icons.Outlined.FavoriteBorder,

                                                contentDescription = null,

                                                tint = Color.Red
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Text(

                                        text = item.category,

                                        color = Color.Gray
                                    )

                                    Spacer(
                                        modifier = Modifier.height(14.dp)
                                    )

                                    Row(

                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        verticalAlignment =
                                            Alignment.CenterVertically,

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween

                                    ) {

                                        Column {

                                            Text(

                                                text =

                                                    if (

                                                        item.price > 0

                                                    )

                                                        "₹${item.price}"

                                                    else

                                                        "Custom Price",

                                                fontSize = 22.sp,

                                                fontWeight =
                                                    FontWeight.ExtraBold,

                                                color =
                                                    Color(0xFFFF6F00)
                                            )

                                            if (

                                                item.variants
                                                    .isNotEmpty()

                                            ) {

                                                Spacer(
                                                    modifier = Modifier.height(4.dp)
                                                )

                                                Text(

                                                    text =
                                                        "${item.variants.size} sizes available",

                                                    color = Color.Gray,

                                                    fontSize = 13.sp
                                                )
                                            }
                                        }

                                        if (

                                            item.variants
                                                .isNotEmpty()

                                        ) {

                                            Button(

                                                onClick = {

                                                    showVariantSheet =
                                                        true
                                                },

                                                colors =
                                                    ButtonDefaults.buttonColors(

                                                        containerColor =
                                                            Color(0xFFFF9800)
                                                    )

                                            ) {

                                                Text("ADD")
                                            }
                                            if (showVariantSheet) {

                                                AlertDialog(

                                                    onDismissRequest = {

                                                        showVariantSheet = false
                                                    },

                                                    confirmButton = {},

                                                    title = {

                                                        Text(
                                                            "Choose Size"
                                                        )
                                                    },

                                                    text = {

                                                        Column {

                                                            item.variants.forEach { variant ->

                                                                Button(

                                                                    onClick = {

                                                                        CartData.addToCart(

                                                                            restaurantId =
                                                                                restaurantId,

                                                                            restaurantName =
                                                                                restaurantId,

                                                                            item = item,

                                                                            selectedVariant =
                                                                                variant
                                                                        )

                                                                        showVariantSheet = false
                                                                    },

                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .padding(vertical = 6.dp)

                                                                ) {

                                                                    Text(

                                                                        "${variant.name} ₹${variant.price}"
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                )
                                            }
                                        }

                                        else if (cartItem == null) {

                                            Button(

                                                onClick = {

                                                    CartData.clearCart()

                                                    CartData.addToCart(

                                                        restaurantId = restaurantId,

                                                        restaurantName = restaurantId,

                                                        item = item
                                                    )
                                                },

                                                colors =
                                                    ButtonDefaults.buttonColors(

                                                        containerColor =
                                                            Color(0xFFFF9800)
                                                    )

                                            ) {

                                                Text(
                                                    text = "ADD"
                                                )
                                            }

                                        } else {

                                            Row(

                                                verticalAlignment =
                                                    Alignment.CenterVertically

                                            ) {

                                                Button(

                                                    onClick = {

                                                        CartData.decrease(
                                                            cartItem
                                                        )
                                                    },

                                                    colors =
                                                        ButtonDefaults.buttonColors(

                                                            containerColor =
                                                                Color(0xFFFF9800)
                                                        ),

                                                    contentPadding =
                                                        PaddingValues(
                                                            horizontal = 12.dp
                                                        )

                                                ) {

                                                    Text("-")
                                                }

                                                Spacer(
                                                    modifier = Modifier.width(10.dp)
                                                )

                                                Text(

                                                    text =
                                                        cartItem.quantity
                                                            .toString(),

                                                    fontWeight =
                                                        FontWeight.Bold,

                                                    fontSize = 18.sp
                                                )

                                                Spacer(
                                                    modifier = Modifier.width(10.dp)
                                                )

                                                Button(

                                                    onClick = {

                                                        CartData.increase(
                                                            cartItem
                                                        )
                                                    },

                                                    colors =
                                                        ButtonDefaults.buttonColors(

                                                            containerColor =
                                                                Color(0xFFFF9800)
                                                        ),

                                                    contentPadding =
                                                        PaddingValues(
                                                            horizontal = 12.dp
                                                        )

                                                ) {

                                                    Text("+")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {

                        Spacer(
                            modifier = Modifier.height(120.dp)
                        )
                    }
                }
            }
        }
    }
}