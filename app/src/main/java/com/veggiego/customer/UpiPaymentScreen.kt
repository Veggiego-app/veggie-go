package com.veggiego.customer

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.ceil

@Composable
fun UpiPaymentScreen(

    navController: NavController,

    totalAmount: Int

) {

    var loading by remember {
        mutableStateOf(false)
    }

    val db = FirebaseFirestore.getInstance()

    var riderPerKm by remember {
        mutableStateOf<Double?>(null)
    }

    var minimumRiderPay by remember {
        mutableStateOf<Double?>(null)
    }

    var restaurantZone by remember {
        mutableStateOf("")
    }

    var commissionPercent by remember {
        mutableStateOf<Double?>(null)
    }

    LaunchedEffect(Unit) {

        db.collection("settings")
            .document("app")
            .get()
            .addOnSuccessListener { doc ->

                riderPerKm =
                    (doc.get("riderPerKm") as? Number)
                        ?.toDouble()

                minimumRiderPay =
                    (doc.get("minimumRiderPay") as? Number)
                        ?.toDouble()
            }
        db.collection("restaurants")
            .document(CartData.currentRestaurantId.value)
            .get()
            .addOnSuccessListener { doc ->

                restaurantZone =
                    doc.getString("zone") ?: ""

                commissionPercent =
                    (doc.get("commissionPercent") as? Number)
                        ?.toDouble()
            }
    }

    Column(

        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .verticalScroll(
                rememberScrollState()
            )
            .padding(20.dp),

        horizontalAlignment =
            Alignment.CenterHorizontally

    ) {

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Text(

            text = "UPI Payment",

            color = Color.White,

            fontSize = 28.sp,

            fontWeight = FontWeight.Bold
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(

            text =
                "Scan QR Using Any UPI App",

            color = Color.LightGray,

            fontSize = 16.sp
        )

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Card(

            shape =
                RoundedCornerShape(24.dp)

        ) {

            Image(

                painter =
                    painterResource(
                        R.drawable.phonepe_qr
                    ),

                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),

                contentScale =
                    ContentScale.Fit
            )
        }

        Spacer(
            modifier = Modifier.height(30.dp)
        )

        Card(

            colors =
                CardDefaults.cardColors(
                    containerColor =
                        Color(0xFF1E1E1E)
                ),

            shape =
                RoundedCornerShape(20.dp)

        ) {

            Column(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally

            ) {

                Text(

                    text = "Amount To Pay",

                    color = Color.Gray,

                    fontSize = 16.sp
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Text(

                    text = "₹$totalAmount",

                    color = Color.White,

                    fontSize = 34.sp,

                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(

            onClick = {

                loading = true

                val orderId =
                    db.collection("orders")
                        .document().id

                val userId =
                    FirebaseAuth.getInstance()
                        .currentUser?.uid ?: ""

                val selectedAddress =
                    AddressData
                        .selectedAddress
                        .value

                val distanceKm =
                    CartData.selectedDistanceKm

                val surgeFee =
                    CartData.surgeFee.value

                val configuredRiderPerKm =
                    riderPerKm

                val configuredMinimumRiderPay =
                    minimumRiderPay

                val finalRiderPay =
                    if (
                        configuredRiderPerKm != null &&
                        configuredMinimumRiderPay != null
                    ) {

                        val calculatedRiderPay =
                            maxOf(
                                configuredMinimumRiderPay,
                                distanceKm * configuredRiderPerKm
                            )

                        ceil(calculatedRiderPay).toInt() +
                                surgeFee

                    } else {

                        null
                    }

                val currentItemTotal =
                    CartData.totalPrice()

                val currentPackagingFee =
                    CartData.packagingFee.value

                val currentCommissionPercent =
                    commissionPercent

                val commissionAmount =
                    currentCommissionPercent?.let { percent ->

                        currentItemTotal *
                                percent / 100.0
                    }

                val restaurantPayout =
                    commissionAmount?.let { amount ->

                        currentItemTotal +
                                currentPackagingFee -
                                amount
                    }

                val order =
                    hashMapOf<String, Any>(

                        "orderId" to orderId,

                        "userId" to userId,

                        "customerName" to
                                (selectedAddress?.fullName ?: ""),

                        "customerPhone" to
                                (selectedAddress?.phone ?: ""),

                        "house" to
                                (selectedAddress?.house ?: ""),

                        "area" to
                                (selectedAddress?.area ?: ""),

                        "city" to
                                (selectedAddress?.city ?: ""),

                        "pincode" to
                                (selectedAddress?.pincode ?: ""),

                        "landmark" to
                                (selectedAddress?.landmark ?: ""),

                        "items" to
                                CartData.items.map { cartItem ->

                                    hashMapOf<String, Any>(

                                        "name" to
                                                cartItem.item.name,

                                        "quantity" to
                                                cartItem.quantity,

                                        "image" to
                                                cartItem.item.image,

                                        "description" to
                                                cartItem.item.description,

                                        "variant" to
                                                (
                                                        cartItem
                                                            .selectedVariant
                                                            ?.name
                                                            ?: ""
                                                        ),

                                        "variantPrice" to
                                                (
                                                        cartItem
                                                            .selectedVariant
                                                            ?.price
                                                            ?: cartItem.item.price
                                                        ),

                                        "itemTotal" to
                                                cartItem.totalPrice()
                                    )
                                },

                        "total" to totalAmount,

                        "itemTotal" to currentItemTotal,

                        "packagingFee" to currentPackagingFee,

                        "commissionSnapshotAvailable" to
                                (currentCommissionPercent != null),

                        "deliveryFee" to CartData.deliveryFee.value,

                        "surgeFee" to CartData.surgeFee.value,

                        "surgeReason" to CartData.surgeReason.value,

                        "distanceKm" to distanceKm,

                        "riderPaySnapshotAvailable" to
                                (
                                        configuredRiderPerKm != null &&
                                                configuredMinimumRiderPay != null &&
                                                finalRiderPay != null
                                        ),

                        "platformFee" to CartData.platformFee.value,

                        "gst" to CartData.gst.value,

                        "gstOnItems" to CartData.gstOnItems.value,

                        "gstOnPackaging" to CartData.gstOnPackaging.value,

                        "gstOnPlatform" to CartData.gstOnPlatform.value,

                        "gstOnDelivery" to CartData.gstOnDelivery.value,

                        "discount" to 0,

                        "tip" to CartData.riderTip.value,

                        "customerLat" to (
                                selectedAddress?.latitude ?: 0.0
                                ),

                        "customerLng" to (
                                selectedAddress?.longitude ?: 0.0
                                ),

                        "riderAssigned" to false,

                        "riderId" to "",

                        "riderName" to "",

                        "riderPhone" to "",

                        "status" to "PENDING",

                        "deliveryStatus" to "PENDING",

                        "paymentMethod" to "QR",

                        "restaurantId" to
                                CartData.currentRestaurantId.value,

                        "restaurantName" to
                                CartData.currentRestaurantName.value,

                        "restaurantZone" to restaurantZone,

                        "timestamp" to
                                System.currentTimeMillis()
                    )

                if (
                    currentCommissionPercent != null &&
                    commissionAmount != null &&
                    restaurantPayout != null
                ) {

                    order["commissionPercent"] =
                        currentCommissionPercent

                    order["commissionAmount"] =
                        commissionAmount

                    order["restaurantPayout"] =
                        restaurantPayout
                }

                if (
                    configuredRiderPerKm != null &&
                    configuredMinimumRiderPay != null &&
                    finalRiderPay != null
                ) {

                    order["riderPerKm"] =
                        configuredRiderPerKm

                    order["minimumRiderPay"] =
                        configuredMinimumRiderPay

                    order["riderPay"] =
                        finalRiderPay
                }

                db.collection("orders")
                    .document(orderId)
                    .set(order)

                    .addOnSuccessListener {

                        loading = false

                        CartData.clearCart()

                        Toast.makeText(

                            navController.context,

                            "QR Order Placed 😎",

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

            shape =
                RoundedCornerShape(18.dp),

            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        Color(0xFF16A34A)
                )

        ) {

            if (loading) {

                CircularProgressIndicator(
                    color = Color.White
                )

            } else {

                Text(

                    text = "I HAVE PAID",

                    fontSize = 18.sp
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedButton(

            onClick = {

                navController.popBackStack()
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),

            shape =
                RoundedCornerShape(18.dp)

        ) {

            Text(
                text = "BACK"
            )
        }
    }
}