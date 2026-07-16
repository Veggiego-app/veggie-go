package com.veggiego.customer

import android.location.Location
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.statusBarsPadding
import com.google.firebase.firestore.Query

@Composable
fun OrdersScreen(
    navController: NavController
) {

    val userId =
        FirebaseAuth.getInstance()
            .currentUser?.uid ?: ""

    var orders by remember {

        mutableStateOf<List<OrderModel>>(
            emptyList()
        )
    }

    var loading by remember {

        mutableStateOf(true)
    }
    var lastStatuses by remember {

        mutableStateOf<Map<String, String>>(
            emptyMap()
        )
    }
    var error by remember {

        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("orders")

            .whereEqualTo(
                "userId",
                userId
            )

            .addSnapshotListener { value, errorSnapshot ->
                if (errorSnapshot != null) {

                    android.util.Log.d(
                        "ORDERS_ERROR",
                        errorSnapshot.message ?: "Unknown"
                    )

                    loading = false
                    error = true

                    return@addSnapshotListener
                }
                if (value != null) {
                    value.documents.forEach { doc ->

                        val newStatus =

                            doc.getString(
                                "status"
                            ) ?: "PENDING"

                        val oldStatus =

                            lastStatuses[doc.id]

                        if (

                            oldStatus != null

                            &&

                            oldStatus != newStatus

                        ) {

                            val message =

                                when (newStatus) {

                                    "APPROVED" ->

                                        "🍕 Your order has been accepted"

                                    "PREPARING" ->

                                        "👨‍🍳 Restaurant is preparing your food"

                                    "READY_FOR_PICKUP" ->

                                        "📦 Order ready for pickup"

                                    "RIDER_ASSIGNED" ->

                                        "🛵 Rider assigned"

                                    "OUT_FOR_DELIVERY" ->

                                        "🚚 Your order is on the way"

                                    "DELIVERED" ->

                                        "✅ Order Delivered"

                                    "CUSTOMER_CANCELLED",
                                    "CANCELLED" ->

                                        "❌ Order Cancelled"

                                    else -> null
                                }

                            if (message != null) {

                                NotificationHelper
                                    .showNotification(

                                        context = navController.context,

                                        title = "VeggieGo",

                                        body = message
                                    )
                            }
                        }
                    }

                    orders =

                        value.documents.map { doc ->

                            val itemsList =

                                try {

                                    val rawItems =
                                        doc.get("items") as? List<*>

                                    rawItems?.mapNotNull { item ->

                                        when (item) {

                                            is Map<*, *> -> {

                                                val qty =
                                                    item["quantity"]
                                                        ?.toString() ?: "1"

                                                val name =
                                                    item["name"]
                                                        ?.toString() ?: "Item"

                                                "$qty x $name"
                                            }

                                            is String -> {

                                                "1 x $item"
                                            }

                                            else -> null
                                        }
                                    } ?: emptyList()

                                } catch (e: Exception) {

                                    emptyList()
                                }

                            OrderModel(

                                id = doc.id,

                                items = itemsList,

                                total =
                                    (doc.getLong("total") ?: 0).toInt(),

                                status =
                                    doc.getString(
                                        "status"
                                    ) ?: "PENDING",

                                timestamp =
                                    doc.getLong(
                                        "timestamp"
                                    ) ?: 0L,

                                restaurantName =

                                    doc.getString(
                                        "restaurantName"
                                    )

                                        ?: doc.getString(
                                            "restaurant"
                                        )

                                        ?: doc.getString(
                                            "shopName"
                                        )

                                        ?: "VeggieGo",

                                restaurantId =
                                    doc.getString(
                                        "restaurantId"
                                    ) ?: "",

                                riderPhone =
                                    doc.getString(
                                        "riderPhone"
                                    ) ?: "",

                                riderName =
                                    doc.getString(
                                        "riderName"
                                    ) ?: ""
                            )
                        }

                            .sortedByDescending {

                                it.timestamp
                            }
                    lastStatuses =

                        orders.associate {

                            it.id to it.status
                        }

                    loading = false
                }
            }
    }

    Box(

        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(Color.Black)

    ) {
        if (error) {

            Box(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(30.dp),

                contentAlignment =
                    Alignment.Center

            ) {

                Column(

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(

                        text = "📡",

                        fontSize = 90.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(20.dp)
                    )

                    Text(

                        text =
                            "No Internet Connection",

                        fontSize = 28.sp,

                        fontWeight =
                            FontWeight.Bold
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(

                        text =
                            "Please check your internet and try again 😎",

                        color = Color.Gray
                    )

                    Spacer(
                        modifier =
                            Modifier.height(30.dp)
                    )

                    Button(

                        onClick = {

                            loading = true

                            error = false
                        },

                        shape =
                            RoundedCornerShape(18.dp)
                    ) {

                        Text(
                            "Retry"
                        )
                    }
                }
            }

        } else
        if (loading) {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize(),

                contentPadding =
                    PaddingValues(16.dp)

            ) {

                items(5) {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 18.dp),

                        shape =
                            RoundedCornerShape(28.dp),

                        colors =
                            CardDefaults.cardColors(

                                containerColor =
                                    Color(0xFFFFFCF4)
                            )

                    ) {

                        ShimmerMenuCard()
                    }
                }
            }

        } else {
            if (orders.isEmpty()) {

                Box(

                    modifier = Modifier
                        .fillMaxSize()
                        .padding(30.dp),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Column(

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(

                            text = "📦",

                            fontSize = 90.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )

                        Text(

                            text =
                                "No Orders Yet",

                            fontSize = 28.sp,

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier =
                                Modifier.height(10.dp)
                        )

                        Text(

                            text =
                                "Your future delicious orders will appear here 😎",

                            color = Color.Gray
                        )

                        Spacer(
                            modifier =
                                Modifier.height(30.dp)
                        )

                        Button(

                            onClick = {

                                navController.navigate(
                                    "home"
                                )
                            },

                            shape =
                                RoundedCornerShape(18.dp)
                        ) {

                            Text(
                                "Start Ordering"
                            )
                        }
                    }
                }

            } else {

                LazyColumn(

                    modifier = Modifier
                        .fillMaxSize(),

                    contentPadding =
                        PaddingValues(16.dp)

                ) {

                    items(orders) { order ->

                        OrderCard(

                            navController =
                                navController,

                            order = order
                        )

                        Spacer(
                            modifier =
                                Modifier.height(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(

    navController: NavController,

    order: OrderModel

) {

    val context = LocalContext.current

    fun calculateDistanceKm(

        startLat: Double,

        startLng: Double,

        endLat: Double,

        endLng: Double

    ): Double {

        val result = FloatArray(1)

        Location.distanceBetween(

            startLat,

            startLng,

            endLat,

            endLng,

            result

        )

        return result[0] / 1000.0
    }

    var unreadCount by remember {

        mutableStateOf(0)
    }

    LaunchedEffect(order.id) {

        FirebaseFirestore
            .getInstance()
            .collection("chats")
            .document(order.id)
            .collection("messages")

            .whereEqualTo(
                "senderType",
                "rider"
            )

            .whereEqualTo(
                "seen",
                false
            )

            .addSnapshotListener { value, _ ->

                unreadCount =
                    value?.documents?.size ?: 0
            }
    }

    val formattedDate =

        remember(order.timestamp) {

            try {

                SimpleDateFormat(

                    "dd MMM, hh:mm a",

                    Locale.getDefault()

                ).format(Date(order.timestamp))

            } catch (e: Exception) {

                ""
            }
        }

    Card(

        modifier = Modifier
            .fillMaxWidth()

            .clickable {

                navController.navigate(

                    "order_details/${order.id}"
                )
            },

        shape =
            RoundedCornerShape(28.dp),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 10.dp
            ),

        border = BorderStroke(
            1.dp,
            Color(0xFFEAEAEA)
        ),

        colors = CardDefaults.cardColors(

            containerColor =
                Color(0xFFFFFCF4)
        )

    ) {

        Column(

            modifier = Modifier
                .padding(20.dp)

        ) {

            Row(

                modifier = Modifier
                    .fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                Column {

                    Text(

                        text = "VeggieGo",

                        fontSize = 15.sp,

                        color = Color.Gray
                    )

                    Text(

                        text =
                            order.restaurantName,

                        fontSize = 30.sp,

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Surface(

                    shape =
                        RoundedCornerShape(50),

                    color =
                        when (order.status) {

                            "PENDING" ->
                                Color(0xFFFFC107)

                            "APPROVED",
                            "ACCEPTED" ->
                                Color(0xFF43A047)

                            "PREPARING" ->
                                Color(0xFFFF9800)

                            "READY" ->
                                Color(0xFF1E88E5)

                            "PICKED_UP" ->
                                Color(0xFF8E24AA)

                            "OUT_FOR_DELIVERY" ->
                                Color(0xFF2962FF)

                            "DELIVERED" ->
                                Color(0xFF2E7D32)

                            "CUSTOMER_CANCELLED",
                            "CANCELLED" ->

                                Color(0xFFD32F2F)

                            else ->
                                Color.Gray
                        }

                ) {

                    Text(

                        text = when (order.status) {

                            "PENDING" ->
                                "Pending"

                            "APPROVED" ->
                                "Accepted"

                            "PREPARING" ->
                                "Preparing"

                            "READY" ->
                                "Ready"

                            "PICKED_UP" ->
                                "Picked Up"

                            "OUT_FOR_DELIVERY" ->
                                "Out For Delivery"

                            "DELIVERED" ->
                                "Delivered"
                            "CUSTOMER_CANCELLED",
                            "CANCELLED" ->

                                "Order Cancelled"

                            else ->
                                order.status
                        },

                        color = Color.White,

                        modifier =
                            Modifier.padding(

                                horizontal = 16.dp,

                                vertical = 8.dp
                            ),

                        fontWeight =
                            FontWeight.Bold
                    )
                }
            }

            Spacer(
                modifier =
                    Modifier.height(12.dp)
            )

            Text(

                text =
                    "Order ID:\n${order.id}",

                color = Color.Gray,

                fontSize = 13.sp
            )

            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(

                text = order.items
                    .take(3)
                    .joinToString("\n"),

                fontSize = 18.sp,

                fontWeight = FontWeight.SemiBold
            )

            if (order.items.size > 3) {

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(

                    text = "+${order.items.size - 3} more items",

                    color = Color.Gray,

                    fontSize = 14.sp
                )
            }

            Spacer(
                modifier =
                    Modifier.height(16.dp)
            )

            Text(

                text = formattedDate,

                color = Color.Gray,

                fontSize = 15.sp
            )
            if (unreadCount > 0) {

                Spacer(
                    modifier =
                        Modifier.height(10.dp)
                )

                Surface(

                    color =
                        Color(0xFFFFEBEE),

                    shape =
                        RoundedCornerShape(50)
                ) {

                    Text(

                        text =

                            if (unreadCount == 1)

                                "🔴 New Message"

                            else

                                "💬 $unreadCount New Messages",

                        color =
                            Color.Red,

                        fontWeight =
                            FontWeight.Bold,

                        modifier =
                            Modifier.padding(

                                horizontal = 14.dp,

                                vertical = 8.dp
                            )
                    )
                }
            }
            Spacer(
                modifier =
                    Modifier.height(18.dp)
            )

            Text(

                text = "₹${order.total}",

                color = Color(0xFF008F39),

                fontWeight =
                    FontWeight.ExtraBold,

                fontSize = 42.sp
            )

            Spacer(
                modifier =
                    Modifier.height(22.dp)
            )

            val showTracking =

                order.status == "PICKED_UP" ||
                        order.status == "OUT_FOR_DELIVERY"

            val riderAssigned =

                (

                        order.riderPhone.isNotEmpty()

                                ||

                                order.riderName.isNotEmpty()

                        )

                        &&

                        order.status != "DELIVERED"

                        &&

                        order.status != "CUSTOMER_CANCELLED"

                        &&

                        order.status != "CANCELLED"
            val showCallRider =

                order.status == "PREPARING"
                        ||
                        order.status == "RIDER_ASSIGNED"
                        ||
                        order.status == "PICKED_UP"
                        ||
                        order.status == "OUT_FOR_DELIVERY"

            val showDeliveredActions =

                order.status == "DELIVERED"
            val showCancelButton =

                order.status == "PENDING"
                        ||
                        order.status == "ACCEPTED"
                        ||
                        order.status == "APPROVED"


            var showCancelDialog by remember {

                mutableStateOf(false)
            }

            if (showTracking) {

                Button(

                    onClick = {

                        navController.navigate(
                            "tracking/${order.id}"
                        )
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),

                    shape =
                        RoundedCornerShape(18.dp)

                ) {

                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        "Track Order"
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )
            }

            if (showCallRider) {

                OutlinedButton(

                    onClick = {

                        if (!riderAssigned) {

                            android.widget.Toast
                                .makeText(

                                    context,

                                    "🛵 Rider Not Assigned Yet",

                                    android.widget.Toast.LENGTH_SHORT

                                )

                                .show()

                            return@OutlinedButton
                        }

                        val intent = Intent(

                            Intent.ACTION_DIAL,

                            Uri.parse(
                                "tel:${order.riderPhone}"
                            )
                        )

                        context.startActivity(intent)
                    },

                    border = BorderStroke(
                        1.dp,
                        Color(0xFF1B5E20)
                    ),

                    colors =
                        ButtonDefaults.outlinedButtonColors(

                            containerColor =
                                Color.White
                        ),

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(18.dp)

                ) {

                    Icon(
                        Icons.Default.Call,
                        contentDescription = null,
                        tint =

                            if (riderAssigned)

                                Color(0xFF1B5E20)

                            else

                                Color.Gray
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(

                        text =

                            if (riderAssigned)

                                "Call Rider"

                            else

                                "Rider Not Assigned",

                        color =

                            if (riderAssigned)

                                Color(0xFF1B5E20)

                            else

                                Color.Gray
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )
            }

            // ✅ CHAT BUTTON

            val showChat =

                order.riderPhone.isNotEmpty()

                        &&

                        order.status != "PENDING"

                        &&

                        order.status != "DELIVERED"

                        &&

                        order.status != "CUSTOMER_CANCELLED"

            if (showChat) {

                OutlinedButton(

                    onClick = {

                        navController.navigate(

                            "chat/${order.id}"
                        )
                    },

                    border = BorderStroke(
                        1.dp,
                        Color(0xFF2962FF)
                    ),

                    colors =
                        ButtonDefaults.outlinedButtonColors(

                            containerColor =
                                Color.White
                        ),

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(18.dp)

                ) {

                    Text(

                        text =

                            if (unreadCount > 0)

                                "💬 Chat ($unreadCount)"

                            else

                                "💬 Chat With Rider",

                        color =
                            Color(0xFF2962FF)
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )
            }
            // ✅ CANCEL ORDER BUTTON

            if (showCancelButton) {

                OutlinedButton(

                    onClick = {

                        showCancelDialog = true
                    },

                    border = BorderStroke(
                        1.dp,
                        Color.Red
                    ),

                    colors =
                        ButtonDefaults.outlinedButtonColors(

                            containerColor =
                                Color.White
                        ),

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    shape =
                        RoundedCornerShape(18.dp)

                ) {

                    Text(

                        text = "Cancel Order",

                        color = Color.Red,

                        fontWeight =
                            FontWeight.Bold
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )
            }
            // ✅ REORDER BUTTON

            OutlinedButton(

                onClick = {

                    val currentAddress =
                        AddressData.selectedAddress.value

                    if (currentAddress == null) {

                        android.widget.Toast
                            .makeText(
                                context,
                                "📍 Please select delivery address first",
                                android.widget.Toast.LENGTH_SHORT
                            )
                            .show()

                        navController.navigate("select_address")

                        return@OutlinedButton
                    }

                    if (order.restaurantId.isEmpty()) {

                        android.widget.Toast
                            .makeText(
                                context,
                                "Restaurant not found",
                                android.widget.Toast.LENGTH_SHORT
                            )
                            .show()

                        return@OutlinedButton
                    }

                    FirebaseFirestore
                        .getInstance()
                        .collection("restaurants")
                        .document(order.restaurantId)
                        .get()
                        .addOnSuccessListener { doc ->

                            val restaurantLat =
                                doc.getDouble("lat") ?: 0.0

                            val restaurantLng =
                                doc.getDouble("lng") ?: 0.0

                            if (
                                restaurantLat == 0.0 ||
                                restaurantLng == 0.0
                            ) {

                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "Restaurant location not available",
                                        android.widget.Toast.LENGTH_SHORT
                                    )
                                    .show()

                                return@addOnSuccessListener
                            }

                            val km =
                                calculateDistanceKm(

                                    restaurantLat,

                                    restaurantLng,

                                    currentAddress.latitude,

                                    currentAddress.longitude
                                )

                            if (km <= 15.0) {

                                navController.navigate(
                                    "restaurant_detail/${Uri.encode(order.restaurantId)}"
                                )

                            } else {

                                android.widget.Toast
                                    .makeText(
                                        context,
                                        "🍃 This restaurant is not available at your current delivery address",
                                        android.widget.Toast.LENGTH_LONG
                                    )
                                    .show()
                            }
                        }
                        .addOnFailureListener {

                            android.widget.Toast
                                .makeText(
                                    context,
                                    "Unable to check restaurant distance",
                                    android.widget.Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                },

                border = BorderStroke(
                    1.dp,
                    Color(0xFF1B5E20)
                ),

                colors =
                    ButtonDefaults.outlinedButtonColors(

                        containerColor =
                            Color.White
                    ),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),

                shape =
                    RoundedCornerShape(18.dp)

            ) {

                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    tint = Color(0xFF1B5E20)
                )

                Spacer(
                    modifier =
                        Modifier.width(8.dp)
                )

                Text(
                    "Reorder",
                    color = Color(0xFF1B5E20)
                )
            }
            // ✅ CANCEL CONFIRM DIALOG

            if (showCancelDialog) {

                AlertDialog(

                    onDismissRequest = {

                        showCancelDialog = false
                    },

                    title = {

                        Text(
                            "Cancel Order?"
                        )
                    },

                    text = {

                        Text(

                            "Are you sure you want to cancel this order?"
                        )
                    },

                    confirmButton = {

                        Button(

                            onClick = {

                                showCancelDialog = false

                                FirebaseFirestore
                                    .getInstance()
                                    .collection("orders")
                                    .document(order.id)

                                    .update(

                                        mapOf(

                                            "status" to "CUSTOMER_CANCELLED",

                                            "deliveryStatus" to "CUSTOMER_CANCELLED",

                                            "cancelReason" to
                                                    "Customer Cancelled"
                                        )
                                    )
                            },

                            colors =
                                ButtonDefaults.buttonColors(

                                    containerColor =
                                        Color.Red
                                )
                        ) {

                            Text("Yes Cancel")
                        }
                    },

                    dismissButton = {

                        OutlinedButton(

                            onClick = {

                                showCancelDialog = false
                            }
                        ) {

                            Text("No")
                        }
                    }
                )
            }
            if (showDeliveredActions) {

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                Row(

                    modifier = Modifier
                        .fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(12.dp)

                ) {

                    OutlinedButton(

                        onClick = {

                            navController.navigate(
                                "rate/${order.id}"
                            )
                        },

                        border = BorderStroke(
                            1.dp,
                            Color(0xFF1B5E20)
                        ),

                        colors =
                            ButtonDefaults.outlinedButtonColors(

                                containerColor =
                                    Color.White
                            ),

                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),

                        shape =
                            RoundedCornerShape(18.dp)

                    ) {

                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF1B5E20)
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text(
                            "Rate",
                            color = Color(0xFF1B5E20)
                        )
                    }

                    Button(

                        onClick = {

                            navController.navigate(
                                "review/${order.id}"
                            )
                        },

                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp),

                        shape =
                            RoundedCornerShape(18.dp),

                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor =
                                    Color(0xFF1B5E20)
                            )

                    ) {

                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(6.dp)
                        )

                        Text("Review")
                    }
                }
            }
        }
    }
}
