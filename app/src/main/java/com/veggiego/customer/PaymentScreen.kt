package com.veggiego.customer

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(

    navController: NavController,

    totalAmount: Int,

    customerName: String,
    customerPhone: String,

    house: String,
    area: String,
    city: String,
    pincode: String,
    landmark: String

) {

    var loading by remember {
        mutableStateOf(false)
    }

    var selectedPaymentMethod by remember {
        mutableStateOf("COD")
    }

    val db = FirebaseFirestore.getInstance()
    var restaurantLat by remember {
        mutableStateOf(0.0)
    }

    var restaurantLng by remember {
        mutableStateOf(0.0)
    }

    var restaurantZone by remember {
        mutableStateOf("")
    }

    var packagingFee by remember {
        mutableStateOf(0)
    }
    var riderPerKm by remember {
        mutableStateOf(10.0)
    }

    var minimumRiderPay by remember {
        mutableStateOf(23.0)
    }
    BackHandler(

        enabled = loading

    ) {

        Toast.makeText(

            navController.context,

            "⏳ Order Processing...",

            Toast.LENGTH_SHORT

        ).show()
    }
    LaunchedEffect(Unit) {

        db.collection("restaurants")
            .document(
                CartData.currentRestaurantId.value
            )
            .get()

            .addOnSuccessListener { doc ->

                restaurantLat =
                    doc.getDouble("lat") ?: 0.0

                restaurantLng =
                    doc.getDouble("lng") ?: 0.0

                packagingFee =
                    doc.getLong("packagingFee")
                        ?.toInt() ?: 0

                restaurantZone =
                    doc.getString("zone") ?: ""
            }

        db.collection("settings")
            .document("app")
            .get()
            .addOnSuccessListener { doc ->

                riderPerKm =
                    (doc.get("riderPerKm") as? Number)
                        ?.toDouble() ?: 10.0

                minimumRiderPay =
                    (doc.get("minimumRiderPay") as? Number)
                        ?.toDouble() ?: 23.0
            }
    }
    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("Payment")
                },

                navigationIcon = {

                    IconButton(

                        onClick = {
                            navController.popBackStack()
                        }

                    ) {

                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),

            horizontalAlignment =
                Alignment.CenterHorizontally

        ) {

            Spacer(
                modifier = Modifier.height(30.dp)
            )

            Text(

                text = "Select Payment Method",

                fontSize = 26.sp,

                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(

                text =
                    "Choose your preferred payment option",

                color = Color.Gray
            )

            Spacer(
                modifier = Modifier.height(40.dp)
            )

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.spacedBy(10.dp)
            ) {

                // ✅ CASH

                Card(

                    modifier = Modifier
                        .weight(1f)

                        .clickable {

                            selectedPaymentMethod = "COD"
                        },

                    shape =
                        RoundedCornerShape(18.dp),

                    border = BorderStroke(

                        2.dp,

                        if (
                            selectedPaymentMethod == "COD"
                        )

                            Color(0xFF16A34A)

                        else

                            Color.LightGray
                    )
                ) {

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            "💵",
                            fontSize = 28.sp
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            "Cash",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "COD",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // ✅ UPI COMING SOON

                Card(

                    modifier = Modifier
                        .weight(1f)

                        .clickable {

                            Toast.makeText(

                                navController.context,

                                "UPI Apps Coming Soon 😎",

                                Toast.LENGTH_SHORT

                            ).show()
                        },

                    shape =
                        RoundedCornerShape(18.dp),

                    border = BorderStroke(
                        2.dp,
                        Color.LightGray
                    )
                ) {

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            "📱",
                            fontSize = 28.sp
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            "UPI",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Coming Soon",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // ✅ QR PAYMENT

                Card(

                    modifier = Modifier
                        .weight(1f)

                        .clickable {

                            selectedPaymentMethod = "QR"
                        },

                    shape =
                        RoundedCornerShape(18.dp),

                    border = BorderStroke(

                        2.dp,

                        if (
                            selectedPaymentMethod == "QR"
                        )

                            Color(0xFF16A34A)

                        else

                            Color.LightGray
                    )
                ) {

                    Column(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            "🔳",
                            fontSize = 28.sp
                        )

                        Spacer(
                            modifier = Modifier.height(6.dp)
                        )

                        Text(
                            "QR Scan",
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            "Scan & Pay",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Card(

                shape = RoundedCornerShape(20.dp),

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)

                ) {

                    Text(
                        "Order Summary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement =
                            Arrangement.SpaceBetween
                    ) {

                        Text("Total")

                        Text(
                            "₹$totalAmount",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(
                modifier = Modifier.height(50.dp)
            )

            Button(
                enabled = !loading,

                onClick = {
                    if (loading) {

                        return@Button
                    }

                    // ✅ QR FLOW

                    if (selectedPaymentMethod == "QR") {
                        val connectivityManager =

                            navController.context
                                .getSystemService(
                                    Context.CONNECTIVITY_SERVICE
                                ) as ConnectivityManager

                        val network =
                            connectivityManager.activeNetwork

                        val capabilities =

                            connectivityManager
                                .getNetworkCapabilities(
                                    network
                                )

                        val isConnected =

                            capabilities?.hasCapability(

                                NetworkCapabilities
                                    .NET_CAPABILITY_VALIDATED

                            ) == true

                        if (!isConnected) {

                            Toast.makeText(

                                navController.context,

                                "📡 No Internet Connection",

                                Toast.LENGTH_LONG

                            ).show()

                            return@Button
                        }

                        navController.navigate(
                            "upi_payment/$totalAmount"
                        )

                        return@Button
                    }

                    // ✅ COD FLOW
                    val connectivityManager =

                        navController.context
                            .getSystemService(
                                Context.CONNECTIVITY_SERVICE
                            ) as ConnectivityManager

                    val network =
                        connectivityManager.activeNetwork

                    val capabilities =

                        connectivityManager
                            .getNetworkCapabilities(
                                network
                            )

                    val isConnected =

                        capabilities?.hasCapability(

                            NetworkCapabilities
                                .NET_CAPABILITY_VALIDATED

                        ) == true

                    if (!isConnected) {

                        Toast.makeText(

                            navController.context,

                            "📡 No Internet Connection",

                            Toast.LENGTH_LONG

                        ).show()

                        return@Button
                    }
                    loading = true

                            val orderId =

                                db.collection("orders")

                                    .document().id


                            val userId =
                                FirebaseAuth.getInstance()
                                    .currentUser?.uid ?: ""

                            val distanceKm =
                                CartData.selectedDistanceKm

                    val surgeFee =
                        CartData.surgeFee.value

                    val riderPay =
                        maxOf(
                            minimumRiderPay,
                            distanceKm * riderPerKm
                        )

                    val finalRiderPay =
                        ceil(riderPay).toInt() +
                                surgeFee

                            val order = hashMapOf(

                                "orderId" to orderId,

                                "userId" to userId,

                                "customerName" to customerName,
                                "customerPhone" to customerPhone,

                                "house" to house,
                                "area" to area,
                                "city" to city,
                                "pincode" to pincode,
                                "landmark" to landmark,

                                "items" to CartData.items.map { cartItem ->

                                    hashMapOf(

                                        "name" to cartItem.item.name,

                                        "quantity" to cartItem.quantity,

                                        "image" to cartItem.item.image,

                                        "description" to cartItem.item.description,

                                        "variant" to (
                                                cartItem.selectedVariant?.name
                                                    ?: ""
                                                ),

                                        "variantPrice" to (
                                                cartItem.selectedVariant?.price
                                                    ?: cartItem.item.price
                                                ),

                                        "addons" to
                                                cartItem.selectedAddons.map {

                                                    hashMapOf(

                                                        "name" to it.name,

                                                        "price" to it.price
                                                    )
                                                },

                                        "itemTotal" to
                                                cartItem.totalPrice(),

                                        "category" to
                                                cartItem.item.category
                                    )
                                },

                                "total" to totalAmount,

                                "itemTotal" to CartData.totalPrice(),

                                "packagingFee" to packagingFee,

                                "deliveryFee" to CartData.deliveryFee.value,

                                "surgeFee" to CartData.surgeFee.value,

                                "surgeReason" to CartData.surgeReason.value,

                                "distanceKm" to distanceKm,

                                "riderPerKm" to riderPerKm,

                                "minimumRiderPay" to minimumRiderPay,

                                "riderPay" to finalRiderPay,

                                "platformFee" to CartData.platformFee.value,

                                "gst" to CartData.gst.value,
                                "gstOnItems" to CartData.gstOnItems.value,

                                "gstOnPackaging" to CartData.gstOnPackaging.value,

                                "gstOnPlatform" to CartData.gstOnPlatform.value,

                                "gstOnDelivery" to CartData.gstOnDelivery.value,

                                "discount" to 0,

                                "tip" to CartData.riderTip.value,

                                "status" to "PENDING",

                                "deliveryStatus" to "PENDING",

                                "paymentMethod" to selectedPaymentMethod,

                                "restaurantId" to
                                        CartData.currentRestaurantId.value,

                                "restaurantName" to
                                        CartData.currentRestaurantName.value,

                                "restaurantZone" to restaurantZone,

                                "restaurantLat" to restaurantLat,

                                "restaurantLng" to restaurantLng,

                                "timestamp" to
                                        System.currentTimeMillis(),

                                "customerLat" to
                                        AddressData.selectedAddress.value?.latitude,

                                "customerLng" to
                                        AddressData.selectedAddress.value?.longitude,

                                "riderId" to "",

                                "riderName" to "",

                                "riderPhone" to "",

                                "riderAssigned" to false
                            )

                            db.collection("orders")
                                .document(orderId)
                                .set(order)

                                .addOnSuccessListener {

                                    loading = false

                                    CartData.clearCart()

                                    Toast.makeText(
                                        navController.context,
                                        "Order Placed",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    navController.navigate(
                                        "success/$orderId"
                                    ) {
                                        popUpTo("cart") {
                                            inclusive = true
                                        }
                                        launchSingleTop = true
                                    }
                                }

                                .addOnFailureListener {

                                    loading = false

                                    Toast.makeText(
                                        navController.context,
                                        "Failed: ${it.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                        }
                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),

                shape = RoundedCornerShape(18.dp)

            ) {

                if (loading) {

                    CircularProgressIndicator(
                        color = Color.White
                    )

                } else {

                    Text(

                        text =

                            when (selectedPaymentMethod) {

                                "COD" ->
                                    "PLACE COD ORDER"

                                "QR" ->
                                    "OPEN QR PAYMENT"

                                else ->
                                    "PLACE ORDER"
                            },

                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}