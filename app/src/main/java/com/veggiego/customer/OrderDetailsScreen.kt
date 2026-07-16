package com.veggiego.customer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
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
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailsScreen(

    navController: NavController,
    orderId: String

) {

    val context = LocalContext.current

    var order by remember {
        mutableStateOf<OrderModel?>(null)
    }
    var itemTotal by remember {
        mutableIntStateOf(0)
    }

    var packagingFee by remember {
        mutableIntStateOf(0)
    }

    var deliveryFee by remember {
        mutableIntStateOf(0)
    }

    var platformFee by remember {
        mutableIntStateOf(0)
    }

    var discount by remember {
        mutableIntStateOf(0)
    }

    var gst by remember {
        mutableDoubleStateOf(0.0)
    }

    var gstOnItems by remember {
        mutableDoubleStateOf(0.0)
    }

    var gstOnPackaging by remember {
        mutableDoubleStateOf(0.0)
    }

    var gstOnPlatform by remember {
        mutableDoubleStateOf(0.0)
    }

    var gstOnDelivery by remember {
        mutableDoubleStateOf(0.0)
    }

    var tip by remember {
        mutableIntStateOf(0)
    }

    var surgeFee by remember {
        mutableIntStateOf(0)
    }

    var surgeReason by remember {
        mutableStateOf("")
    }

    var showPackaging by remember {
        mutableStateOf(false)
    }

    var showPlatform by remember {
        mutableStateOf(false)
    }

    var showGst by remember {
        mutableStateOf(false)
    }

    var loading by remember {
        mutableStateOf(true)
    }

    LaunchedEffect(orderId) {

        FirebaseFirestore
            .getInstance()
            .collection("orders")
            .document(orderId)
            .addSnapshotListener { snapshot, _ ->

                if (
                    snapshot != null &&
                    snapshot.exists()
                ) {

                    val itemsList =

                        try {

                            val rawItems =
                                snapshot.get("items") as? List<*>

                            rawItems?.mapNotNull { item ->

                                when (item) {

                                    is Map<*, *> -> {

                                        val qty =
                                            item["quantity"]?.toString() ?: "1"

                                        val name =
                                            item["name"]?.toString() ?: "Item"

                                        val variant =
                                            item["variant"]?.toString() ?: ""

                                        val price =
                                            item["variantPrice"]?.toString() ?: "0"

                                        val totalPrice =
                                            (price.toIntOrNull() ?: 0) *
                                                    (qty.toIntOrNull() ?: 1)

                                        "$qty x $name|₹$totalPrice|$variant"
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

                    order = OrderModel(

                        id = snapshot.id,

                        items = itemsList,

                        total =
                            (
                                    snapshot.getLong("total")
                                        ?: 0
                                    ).toInt(),

                        status =
                            snapshot.getString("status")
                                ?: "PENDING",

                        timestamp =
                            snapshot.getLong("timestamp")
                                ?: 0L,

                        restaurantName =

                            snapshot.getString(
                                "restaurantName"
                            )

                                ?: snapshot.getString(
                                    "restaurant"
                                )

                                ?: snapshot.getString(
                                    "shopName"
                                )

                                ?: "VeggieGo",

                        restaurantId =
                            snapshot.getString(
                                "restaurantId"
                            ) ?: "",

                        customerName =
                            snapshot.getString(
                                "customerName"
                            ) ?: "",

                        customerPhone =
                            snapshot.getString(
                                "customerPhone"
                            ) ?: "",

                        house =
                            snapshot.getString(
                                "house"
                            ) ?: "",

                        area =
                            snapshot.getString(
                                "area"
                            ) ?: "",

                        city =
                            snapshot.getString(
                                "city"
                            ) ?: "",

                        pincode =
                            snapshot.getString(
                                "pincode"
                            ) ?: "",

                        landmark =
                            snapshot.getString(
                                "landmark"
                            ) ?: "",

                        paymentMethod =
                            snapshot.getString(
                                "paymentMethod"
                            ) ?: "COD",

                        riderName =
                            snapshot.getString(
                                "riderName"
                            ) ?: "",

                        riderPhone =
                            snapshot.getString(
                                "riderPhone"
                            ) ?: "",

                        itemTotal =
                            (
                                    snapshot.getLong(
                                        "itemTotal"
                                    ) ?: 0
                                    ).toInt(),

                        deliveryFee =
                            (
                                    snapshot.getLong(
                                        "deliveryFee"
                                    ) ?: 0
                                    ).toInt(),

                        packagingFee =
                            (
                                    snapshot.getLong(
                                        "packagingFee"
                                    ) ?: 0
                                    ).toInt(),

                        discount =
                            (
                                    snapshot.getLong(
                                        "discount"
                                    ) ?: 0
                                    ).toInt(),

                        platformFee =
                            (
                                    snapshot.getDouble(
                                        "platformFee"
                                    )
                                        ?: snapshot.getLong(
                                            "platformFee"
                                        )?.toDouble()
                                        ?: 0.0
                                    ),

                        gst =
                            (
                                    snapshot.getDouble(
                                        "gst"
                                    )
                                        ?: snapshot.getLong(
                                            "gst"
                                        )?.toDouble()
                                        ?: 0.0
                                    )
                    )
                    itemTotal =
                        snapshot.getLong("itemTotal")
                            ?.toInt() ?: 0

                    packagingFee =
                        snapshot.getLong("packagingFee")
                            ?.toInt() ?: 0

                    deliveryFee =
                        snapshot.getLong("deliveryFee")
                            ?.toInt() ?: 0

                    surgeFee =
                        snapshot.getLong("surgeFee")
                            ?.toInt() ?: 0

                    surgeReason =
                        snapshot.getString("surgeReason")
                            ?: ""

                    platformFee =
                        snapshot.getLong("platformFee")
                            ?.toInt() ?: 0

                    discount =
                        snapshot.getLong("discount")
                            ?.toInt() ?: 0

                    gst =
                        snapshot.getDouble("gst")
                            ?: 0.0

                    gstOnItems =
                        snapshot.getDouble("gstOnItems")
                            ?: 0.0

                    gstOnPackaging =
                        snapshot.getDouble("gstOnPackaging")
                            ?: 0.0

                    gstOnPlatform =
                        snapshot.getDouble("gstOnPlatform")
                            ?: 0.0

                    gstOnDelivery =
                        snapshot.getDouble("gstOnDelivery")
                            ?: 0.0

                    tip =
                        snapshot.getLong("tip")
                            ?.toInt() ?: 0
                }

                loading = false
            }
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Order Details",
                        fontWeight =
                            FontWeight.Bold
                    )
                },

                navigationIcon = {

                    IconButton(

                        onClick = {
                            navController.popBackStack()
                        }

                    ) {

                        Icon(

                            imageVector =
                                Icons.AutoMirrored
                                    .Filled
                                    .ArrowBack,

                            contentDescription = ""
                        )
                    }
                }
            )
        }

    ) { padding ->

        if (loading) {

            Box(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),

                contentAlignment =
                    Alignment.Center

            ) {

                CircularProgressIndicator()
            }

        } else {

            val currentOrder = order

            if (currentOrder == null) {

                Box(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(padding),

                    contentAlignment =
                        Alignment.Center

                ) {

                    Text(
                        text = "Order not found"
                    )
                }

            } else {

                LazyColumn(

                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Color(0xFFF8F8F8)
                            )
                            .padding(padding),

                    contentPadding =
                        PaddingValues(16.dp),

                    verticalArrangement =
                        Arrangement.spacedBy(14.dp)

                ) {

                    item {

                        Card(

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )

                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                            ) {

                                Text(

                                    text =
                                        "Order #$orderId",

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 20.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(10.dp)
                                )

                                Row(

                                    verticalAlignment =
                                        Alignment.CenterVertically

                                ) {

                                    Icon(

                                        imageVector =
                                            Icons.Default.Restaurant,

                                        contentDescription = "",

                                        tint =
                                            Color(0xFF2E7D32)
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(8.dp)
                                    )

                                    Text(

                                        text =
                                            currentOrder.restaurantName,

                                        fontWeight =
                                            FontWeight.SemiBold
                                    )
                                }

                                Spacer(
                                    modifier =
                                        Modifier.height(10.dp)
                                )

                                Text(

                                    text =
                                        formatTime(
                                            currentOrder.timestamp
                                        ),

                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    item {

                        StatusCard(
                            status =
                                currentOrder.status
                        )
                    }

                    item {

                        Card(

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )

                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                            ) {

                                Text(

                                    text =
                                        "Delivery Address",

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 18.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(12.dp)
                                )

                                Text(

                                    text =
                                        currentOrder.customerName,

                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        currentOrder.customerPhone
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(8.dp)
                                )

                                Text(
                                    text =
                                        currentOrder.house
                                )

                                Text(
                                    text =
                                        currentOrder.area
                                )

                                Text(

                                    text =
                                        currentOrder.city +
                                                " - " +
                                                currentOrder.pincode
                                )

                                if (
                                    currentOrder.landmark
                                        .isNotEmpty()
                                ) {

                                    Spacer(
                                        modifier =
                                            Modifier.height(4.dp)
                                    )

                                    Text(

                                        text =
                                            "Landmark: " +
                                                    currentOrder.landmark
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Card(

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )

                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                            ) {

                                Text(

                                    text =
                                        "Rider Details",

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 18.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(12.dp)
                                )

                                Text(

                                    text =
                                        currentOrder.riderName
                                            .ifEmpty {
                                                "Rider not assigned"
                                            },

                                    fontWeight =
                                        FontWeight.SemiBold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(6.dp)
                                )

                                Text(

                                    text =
                                        currentOrder.riderPhone
                                            .ifEmpty {
                                                "Phone unavailable"
                                            }
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(14.dp)
                                )

                                Button(

                                    onClick = {

                                        if (
                                            currentOrder
                                                .riderPhone
                                                .isNotEmpty()
                                        ) {

                                            val intent =
                                                Intent(
                                                    Intent.ACTION_DIAL
                                                )

                                            intent.data =
                                                Uri.parse(
                                                    "tel:${currentOrder.riderPhone}"
                                                )

                                            context.startActivity(
                                                intent
                                            )
                                        }
                                    },

                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                Color(0xFF2E7D32)
                                        ),

                                    shape =
                                        RoundedCornerShape(14.dp)

                                ) {

                                    Icon(

                                        imageVector =
                                            Icons.Default.Call,

                                        contentDescription = ""
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.width(8.dp)
                                    )

                                    Text(
                                        text = "Call Rider"
                                    )

                                }
                                Spacer(
                                    modifier =
                                        Modifier.height(10.dp)
                                )

                                OutlinedButton(

                                    onClick = {

                                        navController.navigate(
                                            "tracking/$orderId"
                                        )
                                    },

                                    modifier =
                                        Modifier.fillMaxWidth(),

                                    shape =
                                        RoundedCornerShape(14.dp)

                                ) {

                                    Text(
                                        text = "🚚 Track Rider"
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Card(

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )

                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                            ) {

                                Text(

                                    text =
                                        "Ordered Items",

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 18.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(14.dp)
                                )

                                if (
                                    currentOrder.items.isNotEmpty()
                                ) {

                                    currentOrder.items
                                        .forEach { item ->

                                            Row(

                                                modifier =
                                                    Modifier
                                                        .fillMaxWidth()
                                                        .padding(
                                                            vertical = 8.dp
                                                        ),

                                                horizontalArrangement =
                                                    Arrangement.SpaceBetween

                                            ) {

                                                val parts = item.split("|")

                                                Column(
                                                    modifier = Modifier.weight(1f)
                                                ) {

                                                    Text(
                                                        text = parts[0]
                                                    )

                                                    if (
                                                        parts.size > 2 &&
                                                        parts[2].isNotEmpty()
                                                    ) {

                                                        Text(
                                                            text = parts[2],
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }

                                                Text(
                                                    text = parts[1],
                                                    fontWeight = FontWeight.Bold
                                                )

                                            }

                                            HorizontalDivider()
                                        }

                                } else {

                                    Text(
                                        text = "No items found"
                                    )
                                }
                            }
                        }
                    }

                    item {

                        Card(

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )

                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                            ) {

                                Text(

                                    text =
                                        "Bill Details",

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 18.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(14.dp)
                                )

                                BillRow(
                                    "Item Total",
                                    "₹${currentOrder.itemTotal}"
                                )

                                BillRow(
                                    "Delivery Fee",
                                    "₹${currentOrder.deliveryFee}"
                                )

                                if (surgeFee > 0) {

                                    BillRow(

                                        if (surgeReason.isBlank())

                                            "⚡ Surge Fee"

                                        else

                                            "⚡ Surge Fee ($surgeReason)",

                                        "₹$surgeFee"

                                    )
                                }

                                Column {

                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween

                                    ) {

                                        Text("Restaurant Packaging Charges")

                                        TextButton(

                                            onClick = {
                                                showPackaging =
                                                    !showPackaging
                                            }

                                        ) {
                                            Text(
                                                if (showPackaging) "▲"
                                                else "▼"
                                            )
                                        }

                                        Text("₹$packagingFee")
                                    }

                                    if (showPackaging) {

                                        Text(

                                            text =
                                                "Packaging charges are decided by restaurant",

                                            color = Color.Gray,

                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Column {

                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween

                                    ) {

                                        Text("Platform Fee")

                                        TextButton(

                                            onClick = {
                                                showPlatform =
                                                    !showPlatform
                                            }

                                        ) {
                                            Text(
                                                if (showPlatform) "▲"
                                                else "▼"
                                            )
                                        }

                                        Text("₹$platformFee")
                                    }

                                    if (showPlatform) {

                                        Text(

                                            text =
                                                "This fee helps keep VeggieGo running",

                                            color = Color.Gray,

                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                Column {

                                    Row(

                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),

                                        horizontalArrangement =
                                            Arrangement.SpaceBetween

                                    ) {

                                        Text("GST (Govt Taxes)")

                                        TextButton(

                                            onClick = {
                                                showGst =
                                                    !showGst
                                            }

                                        ) {
                                            Text(
                                                if (showGst) "▲"
                                                else "▼"
                                            )
                                        }

                                        Text(
                                            "₹${String.format("%.2f", gst)}"
                                        )
                                    }

                                    if (showGst) {

                                        BillRow(
                                            "GST on items",
                                            "₹${String.format("%.2f", gstOnItems)}"
                                        )

                                        BillRow(
                                            "GST on packaging",
                                            "₹${String.format("%.2f", gstOnPackaging)}"
                                        )

                                        BillRow(
                                            "GST on platform fee",
                                            "₹${String.format("%.2f", gstOnPlatform)}"
                                        )

                                        BillRow(
                                            "GST on delivery fee",
                                            "₹${String.format("%.2f", gstOnDelivery)}"
                                        )
                                    }
                                }
                                if (tip > 0) {

                                    BillRow(
                                        "Tip For Rider",
                                        "₹$tip"
                                    )
                                }
                                BillRow(
                                    "Discount",
                                    "- ₹${currentOrder.discount}"
                                )

                                HorizontalDivider(
                                    modifier =
                                        Modifier.padding(
                                            vertical = 12.dp
                                        )
                                )

                                val grandTotal =

                                    itemTotal +
                                            packagingFee +
                                            deliveryFee +
                                            surgeFee +
                                            platformFee +
                                            gst +
                                            tip -
                                            discount

                                BillRow(

                                    "To Pay",

                                    "₹${String.format("%.2f", grandTotal)}",

                                    true
                                )
                            }
                        }
                    }

                    item {

                        Card(

                            shape =
                                RoundedCornerShape(20.dp),

                            colors =
                                CardDefaults.cardColors(
                                    containerColor =
                                        Color.White
                                )

                        ) {

                            Column(

                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp)

                            ) {

                                Text(

                                    text = "Payment",

                                    fontWeight =
                                        FontWeight.Bold,

                                    fontSize = 18.sp
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(10.dp)
                                )

                                Text(

                                    text =
                                        currentOrder.paymentMethod
                                            .ifEmpty {
                                                "Cash on Delivery"
                                            },

                                    color =
                                        Color(0xFF2E7D32),

                                    fontWeight =
                                        FontWeight.Bold
                                )
                            }
                        }
                    }

                    item {

                        Spacer(
                            modifier =
                                Modifier.height(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    status: String
) {

    val color =

        when (status) {

            "PENDING" ->
                Color(0xFFFF9800)

            "PREPARING" ->
                Color(0xFF1976D2)

            "READY" ->
                Color(0xFF7B1FA2)

            "OUT_FOR_DELIVERY" ->
                Color(0xFF2962FF)

            "DELIVERED" ->
                Color(0xFF2E7D32)

            else ->
                Color.Gray
        }

    val icon =

        when (status) {

            "PREPARING" ->
                Icons.Default.Restaurant

            "READY",
            "OUT_FOR_DELIVERY" ->
                Icons.Default.LocalShipping

            "DELIVERED" ->
                Icons.Default.CheckCircle

            else ->
                Icons.Default.Schedule
        }

    val text =

        when (status) {

            "PENDING" ->
                "Pending"

            "PREPARING" ->
                "Preparing"

            "READY" ->
                "Ready"

            "OUT_FOR_DELIVERY" ->
                "Out For Delivery"

            "DELIVERED" ->
                "Delivered"

            else ->
                status
        }

    Card(

        shape =
            RoundedCornerShape(20.dp),

        colors =
            CardDefaults.cardColors(

                containerColor =
                    color.copy(alpha = 0.1f)
            )

    ) {

        Row(

            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(18.dp),

            verticalAlignment =
                Alignment.CenterVertically

        ) {

            Icon(

                imageVector = icon,

                contentDescription = "",

                tint = color
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column {

                Text(

                    text = "Order Status",

                    color = Color.Gray
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(

                    text = text,

                    color = color,

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
fun BillRow(

    title: String,
    value: String,
    bold: Boolean = false

) {

    Row(

        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween

    ) {

        Text(

            text = title,

            fontWeight =

                if (bold)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )

        Text(

            text = value,

            fontWeight =

                if (bold)
                    FontWeight.Bold
                else
                    FontWeight.Normal
        )
    }
}

fun formatTime(
    timestamp: Long
): String {

    return try {

        val sdf =

            SimpleDateFormat(
                "dd MMM yyyy, hh:mm a",
                Locale.getDefault()
            )

        sdf.format(
            Date(timestamp)
        )

    } catch (e: Exception) {

        ""
    }
}