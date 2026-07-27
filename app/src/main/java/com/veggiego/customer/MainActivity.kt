package com.veggiego.customer

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        // ✅ NOTIFICATION PERMISSION

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            ActivityCompat.requestPermissions(

                this,

                arrayOf(

                    Manifest.permission.POST_NOTIFICATIONS,

                    Manifest.permission.ACCESS_FINE_LOCATION,

                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),

                101
            )
        }

        // ✅ GET FCM TOKEN

        FirebaseMessaging
            .getInstance()
            .token

            .addOnSuccessListener { token ->

                android.util.Log.d(
                    "FCM_TOKEN",
                    token
                )

                val userId =

                    FirebaseAuth
                        .getInstance()
                        .currentUser
                        ?.uid
                        ?: return@addOnSuccessListener

                FirebaseFirestore
                    .getInstance()
                    .collection("users")
                    .document(userId)

                    .set(

                        mapOf(
                            "fcmToken" to token
                        ),

                        com.google.firebase.firestore.SetOptions.merge()
                    )
            }

        setContent {

            val navController =
                rememberNavController()

            Surface(
                color =
                    MaterialTheme
                        .colorScheme
                        .background
            ) {

                NavHost(

                    navController =
                        navController,

                    startDestination =
                        if (
                            FirebaseAuth
                                .getInstance()
                                .currentUser != null
                        )
                            "home"
                        else
                            "login"

                ) {

                    composable(
                        "login"
                    ) {

                        LoginScreen(
                            navController
                        )
                    }

                    composable(
                        "home"
                    ) {

                        HomeScreen(
                            navController
                        )
                    }

                    composable(
                        "search/{query}"
                    ) {

                        val query =
                            it.arguments
                                ?.getString(
                                    "query"
                                ) ?: ""

                        SearchResultScreen(

                            navController =
                                navController,

                            query = query
                        )
                    }

                    composable(
                        "food_restaurants/{foodName}"
                    ) {

                        val foodName =
                            it.arguments
                                ?.getString(
                                    "foodName"
                                ) ?: ""

                        FoodRestaurantScreen(

                            navController =
                                navController,

                            foodName = foodName
                        )
                    }

                    composable(
                        "restaurant_detail/{restaurantId}"
                    ) {

                        val restaurantId =
                            it.arguments
                                ?.getString(
                                    "restaurantId"
                                ) ?: ""

                        RestaurantDetailScreen(

                            navController =
                                navController,

                            restaurantId =
                                restaurantId
                        )
                    }

                    composable(
                        "restaurant_detail/{restaurantId}/{focusItem}"
                    ) {

                        val restaurantId =
                            it.arguments
                                ?.getString(
                                    "restaurantId"
                                ) ?: ""

                        val focusItem =
                            it.arguments
                                ?.getString(
                                    "focusItem"
                                ) ?: ""

                        RestaurantDetailScreen(

                            navController =
                                navController,

                            restaurantId =
                                restaurantId,

                            focusItem =
                                focusItem
                        )
                    }

                    composable(
                        "cart"
                    ) {

                        CartScreen(
                            navController
                        )
                    }

                    composable(
                        "payment"
                    ) {

                        PaymentScreen(

                            navController = navController,

                            totalAmount = (

                                    CartData.totalPrice()

                                            +

                                            CartData.packagingFee.value

                                            +

                                            CartData.deliveryFee.value

                                            +

                                            CartData.surgeFee.value

                                            +

                                            CartData.platformFee.value

                                            +

                                            CartData.gst.value

                                            +

                                            CartData.riderTip.value

                                    ).toInt(),


                            customerName =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.fullName ?: "",

                            customerPhone =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.phone ?: "",

                            house =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.house ?: "",

                            area =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.area ?: "",

                            city =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.city ?: "",

                            pincode =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.pincode ?: "",

                            landmark =
                                AddressData
                                    .selectedAddress
                                    .value
                                    ?.landmark ?: ""

                        )
                    }
                    composable(
                        "upi_payment/{totalAmount}"
                    ) {

                        val totalAmount =

                            it.arguments
                                ?.getString(
                                    "totalAmount"
                                )
                                ?.toIntOrNull()
                                ?: 0

                        UpiPaymentScreen(

                            navController =
                                navController,

                            totalAmount =
                                totalAmount
                        )
                    }
                    composable(
                        "success/{orderId}"
                    ) {

                        val orderId =
                            it.arguments
                                ?.getString(
                                    "orderId"
                                ) ?: ""

                        SuccessScreen(

                            navController =
                                navController,

                            orderId = orderId
                        )
                    }

                    composable(
                        "orders"
                    ) {

                        OrdersScreen(
                            navController
                        )
                    }

                    composable(
                        "profile"
                    ) {

                        ProfileScreen(
                            navController
                        )
                    }

                    composable(
                        "favorites"
                    ) {

                        FavoritesScreen(
                            navController
                        )
                    }

                    composable(
                        "tracking/{orderId}"
                    ) {

                        val orderId =

                            it.arguments
                                ?.getString(
                                    "orderId"
                                ) ?: ""

                        TrackingScreen(
                            navController,
                            orderId
                        )
                    }

                    composable(
                        "chat/{orderId}"
                    ) {

                        val orderId =

                            it.arguments
                                ?.getString(
                                    "orderId"
                                ) ?: ""

                        ChatScreen(

                            navController,

                            orderId
                        )
                    }

                    composable(
                        "rate/{orderId}"
                    ) {

                        val orderId =

                            it.arguments
                                ?.getString(
                                    "orderId"
                                ) ?: ""

                        RatingScreen(
                            navController,
                            orderId
                        )
                    }

                    composable(
                        "review/{orderId}"
                    ) {

                        val orderId =

                            it.arguments
                                ?.getString(
                                    "orderId"
                                ) ?: ""

                        ReviewScreen(
                            navController,
                            orderId
                        )
                    }

                    composable(
                        "order_details/{orderId}"
                    ) {

                        val orderId =

                            it.arguments?.getString(
                                "orderId"
                            ) ?: ""

                        OrderDetailsScreen(

                            navController =
                                navController,

                            orderId =
                                orderId
                        )
                    }

                    composable(

                        route =
                            "add_address?addressId={addressId}",

                        arguments = listOf(

                            navArgument(
                                "addressId"
                            ) {

                                nullable = true

                                defaultValue = ""
                            }
                        )

                    ) {

                        val addressId =

                            it.arguments?.getString(
                                "addressId"
                            ) ?: ""

                        AddAddressScreen(
                            navController =
                                navController,

                            addressId =
                                addressId
                        )
                    }

                    composable(

                        route =
                            "select_address?from={from}",

                        arguments = listOf(

                            navArgument(
                                "from"
                            ) {

                                defaultValue = "home"
                            }
                        )

                    ) { backStackEntry ->

                        val from =

                            backStackEntry
                                .arguments
                                ?.getString("from")
                                ?: "home"

                        AddressScreen(

                            navController =
                                navController,

                            from =
                                from
                        )
                    }
                    composable(

                        route =
                            "map_picker?openForm={openForm}",

                        arguments = listOf(

                            navArgument(
                                "openForm"
                            ) {

                                defaultValue = false
                            }
                        )

                    ) { backStackEntry ->

                        val openForm =

                            backStackEntry
                                .arguments
                                ?.getBoolean("openForm")
                                ?: false

                        MapPickerScreen(

                            navController = navController,

                            openForm = openForm
                        )
                    }
                }
            }
        }
    }
}