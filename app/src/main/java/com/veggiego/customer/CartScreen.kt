package com.veggiego.customer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.DocumentSnapshot
import kotlin.math.ceil
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    navController: NavController
) {

    val cartItems = CartData.items

    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()

    val restaurantName =
        CartData.currentRestaurantName.value

    val itemTotal =
        CartData.totalPrice()

    var packagingCharge by remember {
        mutableIntStateOf(0)
    }
    var maxDeliveryDistance by remember {
        mutableDoubleStateOf(15.0)
    }

    LaunchedEffect(Unit) {

        val restaurantId =
            CartData.currentRestaurantId.value

        if (restaurantId.isNotBlank()) {

            db.collection("restaurants")
                .document(restaurantId)
                .get()
                .addOnSuccessListener { doc ->

                    packagingCharge =
                        doc.getLong("packagingFee")
                            ?.toInt() ?: 0

                    maxDeliveryDistance =

                        doc.getDouble("maxDeliveryDistance")
                            ?: 15.0

                    CartData.packagingFee.value =
                        packagingCharge
                }
        }
    }

    val address =
        CartData.selectedAddress

    val distanceKm =
        CartData.selectedDistanceKm

    val deliveryTime =
        CartData.deliveryTime

    val calculatingRoute =
        CartData.calculatingRoute

    var deliveryCharge by remember {
        mutableIntStateOf(0)
    }

    var deliveryPerKm by remember {
        mutableIntStateOf(10)
    }

    var platformFee by remember {

        mutableIntStateOf(5)

    }

    var gstPercentage by remember {

        mutableDoubleStateOf(0.0)

    }

    var minimumOrder by remember {

        mutableIntStateOf(100)

    }

    var freeDeliveryRules by remember {
        mutableStateOf<List<Map<String, Any>>>(emptyList())
    }
    var surgeSlots by remember {
        mutableStateOf<List<Map<String, Any>>>(emptyList())
    }

    var surgeFee by remember {
        mutableIntStateOf(0)
    }

    var surgeReason by remember {
        mutableStateOf("")
    }

    LaunchedEffect(Unit){

        db.collection("settings")

            .document("app")

            .get()

            .addOnSuccessListener { doc ->

                platformFee =
                    doc.getLong("platformFee")
                        ?.toInt()
                        ?: 5

                gstPercentage =
                    (doc.getLong("gstPercentage")
                        ?: 0).toDouble()

                minimumOrder =
                    doc.getLong("minimumOrder")
                        ?.toInt()
                        ?: 100

                deliveryPerKm =
                    doc.getLong("deliveryPerKm")
                        ?.toInt()
                        ?: 10
                freeDeliveryRules =

                    doc.get("freeDeliveryRules")
                            as? List<Map<String, Any>>

                        ?: emptyList()
                surgeSlots =
                    doc.get("surgeSlots")
                            as? List<Map<String, Any>>
                        ?: emptyList()

            }

    }
    LaunchedEffect(
        AddressData.selectedAddress.value,
        CartData.currentRestaurantId.value
    ) {

        val address = AddressData.selectedAddress.value
        val restaurantId = CartData.currentRestaurantId.value

        if (address == null || restaurantId.isBlank()) {
            return@LaunchedEffect
        }

        CartData.calculatingRoute = true

        db.collection("restaurants")
            .document(restaurantId)
            .get()
            .addOnSuccessListener { doc ->

                val restaurantLat =
                    doc.getDouble("lat") ?: 0.0

                val restaurantLng =
                    doc.getDouble("lng") ?: 0.0

                kotlinx.coroutines.CoroutineScope(
                    kotlinx.coroutines.Dispatchers.Main
                ).launch {

                    val result =
                        RoadDistanceRepository.getRoadDistance(

                            restaurantLat,
                            restaurantLng,

                            address.latitude,
                            address.longitude

                        )

                    CartData.selectedDistanceKm =
                        result.first

                    CartData.deliveryTime =
                        result.second

                    CartData.calculatingRoute =
                        false
                }

            }
    }
    LaunchedEffect(
        itemTotal,
        distanceKm,
        deliveryPerKm,
        freeDeliveryRules
    ) {
        var freeKm = 0

        freeDeliveryRules.forEach { rule ->

            val minOrder =

                (rule["minOrder"] as? Long)
                    ?.toInt()
                    ?: 0

            val km =

                (rule["freeKm"] as? Long)
                    ?.toInt()
                    ?: 0

            if (itemTotal >= minOrder) {

                if (km > freeKm) {

                    freeKm = km

                }

            }

        }

        val extraKm =

            (distanceKm - freeKm)
                .coerceAtLeast(0.0)

        deliveryCharge =
            (extraKm * deliveryPerKm).toInt()

        CartData.deliveryFee.value =
            deliveryCharge
    }

    LaunchedEffect(surgeSlots) {

        surgeFee = 0
        surgeReason = ""

        val calendar = java.util.Calendar.getInstance()

        val currentMinutes =
            calendar.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                    calendar.get(java.util.Calendar.MINUTE)

        surgeSlots.forEach { slot ->

            val start =
                slot["start"]?.toString() ?: return@forEach

            val end =
                slot["end"]?.toString() ?: return@forEach

            val amount =
                (slot["amount"] as? Long)?.toInt()
                    ?: (slot["amount"] as? Double)?.toInt()
                    ?: 0

            val startParts = start.split(":")
            val endParts = end.split(":")

            if (
                startParts.size != 2 ||
                endParts.size != 2
            ) return@forEach

            val startMinutes =
                startParts[0].toInt() * 60 +
                        startParts[1].toInt()

            val endMinutes =
                endParts[0].toInt() * 60 +
                        endParts[1].toInt()

            val active =

                if (startMinutes <= endMinutes) {

                    currentMinutes in startMinutes until endMinutes

                } else {

                    currentMinutes >= startMinutes ||
                            currentMinutes < endMinutes

                }

            if (active) {

                surgeFee = amount

                surgeReason =
                    slot["reason"]?.toString() ?: ""

                CartData.surgeFee.value = surgeFee

                CartData.surgeReason.value = surgeReason

                return@LaunchedEffect
            }
        }
        CartData.surgeFee.value = surgeFee
        CartData.surgeReason.value = surgeReason
    }

    val gstOnItems =
        itemTotal * (gstPercentage / 100)

    val gstOnPackaging =
        packagingCharge * (gstPercentage / 100)

    val gstOnPlatform =
        platformFee * (gstPercentage / 100)

    val gstOnDelivery =
        deliveryCharge * (gstPercentage / 100)

    val gst =
        gstOnItems +
                gstOnPackaging +
                gstOnPlatform +
                gstOnDelivery
    CartData.platformFee.value = platformFee

    CartData.gst.value = gst

    CartData.gstOnItems.value = gstOnItems

    CartData.gstOnPackaging.value = gstOnPackaging

    CartData.gstOnPlatform.value = gstOnPlatform

    CartData.gstOnDelivery.value = gstOnDelivery

    CartData.packagingFee.value = packagingCharge


    var riderTip by remember {
        mutableStateOf(0)
    }
    var customTip by remember {
        mutableStateOf("")
    }

    val grandTotal =
        itemTotal +
                packagingCharge +
                deliveryCharge +
                surgeFee +
                platformFee +
                gst +
                riderTip

    var instruction by remember {

        mutableStateOf("")
    }

    var gstExpand by remember {
        mutableStateOf(false)
    }

    var deliveryExpand by remember {
        mutableStateOf(false)
    }

    var platformExpand by remember {
        mutableStateOf(false)
    }

    var showUnavailableDialog by remember {
        mutableStateOf(false)
    }

    var unavailableMessage by remember {
        mutableStateOf("")
    }

    var isPlacingOrder by remember {
        mutableStateOf(false)
    }

    if (showUnavailableDialog) {

        AlertDialog(

            onDismissRequest = {
                showUnavailableDialog = false
            },

            title = {
                Text(
                    text = "Unable to Place Order",
                    fontWeight = FontWeight.Bold
                )
            },

            text = {
                Text(unavailableMessage)
            },

            confirmButton = {

                TextButton(

                    onClick = {
                        showUnavailableDialog = false
                    }

                ) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(

        topBar = {

            if (cartItems.isNotEmpty()) {

                TopAppBar(

                    title = {

                        Column {

                            Text(
                                text = restaurantName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )

                            Text(
                                text = "$deliveryTime to Home",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
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
                    },

                    actions = {

                        IconButton(
                            onClick = {

                                CartData.clearCart()

                                navController.navigate(
                                    "home"
                                ) {

                                    popUpTo(0)
                                }
                            }
                        ) {

                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {

            if (cartItems.isNotEmpty()) {

                Surface(
                    shadowElevation = 10.dp,
                    color = Color.White
                ) {

                    Row(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),

                        horizontalArrangement =
                            Arrangement.SpaceBetween,

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Column {

                            Text(
                                text =
                                    "₹%.2f".format(
                                        grandTotal
                                    ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )

                            Text(
                                text = "TOTAL",
                                color = Color.Gray
                            )
                        }

                        Button(

                            enabled = !calculatingRoute && !isPlacingOrder,

                            onClick = {

                                if (isPlacingOrder) {
                                    return@Button
                                }

                                if (itemTotal < minimumOrder) {

                                    Toast.makeText(
                                        context,
                                        "Minimum order amount is ₹$minimumOrder",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    return@Button
                                }

                                val addressData =
                                    AddressData.selectedAddress.value

                                if (
                                    addressData == null ||
                                    addressData.fullName.isBlank() ||
                                    addressData.phone.isBlank() ||
                                    addressData.house.isBlank() ||
                                    addressData.area.isBlank() ||
                                    addressData.city.isBlank() ||
                                    addressData.pincode.isBlank() ||
                                    addressData.latitude == 0.0 ||
                                    addressData.longitude == 0.0
                                ) {

                                    Toast.makeText(
                                        context,
                                        "❌ Please complete your delivery address",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    navController.navigate(
                                        "select_address?from=cart"
                                    )

                                    return@Button
                                }

                                val restaurantId =
                                    CartData.currentRestaurantId.value

                                if (restaurantId.isBlank()) {

                                    Toast.makeText(
                                        context,
                                        "Restaurant information is missing. Please add the items again.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    return@Button
                                }

                                isPlacingOrder = true

                                db.collection("restaurants")
                                    .document(restaurantId)
                                    .get()
                                    .addOnSuccessListener { doc ->

                                        if (!doc.exists()) {

                                            isPlacingOrder = false

                                            unavailableMessage =
                                                "This restaurant is no longer available."

                                            showUnavailableDialog = true
                                            return@addOnSuccessListener
                                        }

                                        val closedReason =
                                            getRestaurantClosedReason(doc)

                                        if (closedReason != null) {

                                            isPlacingOrder = false
                                            unavailableMessage = closedReason
                                            showUnavailableDialog = true

                                            return@addOnSuccessListener
                                        }

                                        val restaurantLat =
                                            doc.getDouble("lat") ?: 0.0

                                        val restaurantLng =
                                            doc.getDouble("lng") ?: 0.0

                                        if (
                                            restaurantLat == 0.0 ||
                                            restaurantLng == 0.0
                                        ) {

                                            isPlacingOrder = false
                                            unavailableMessage =
                                                "Restaurant location is unavailable. Please try again later."
                                            showUnavailableDialog = true

                                            return@addOnSuccessListener
                                        }

                                        val freshMaxDistance =
                                            doc.getDouble("maxDeliveryDistance")
                                                ?: maxDeliveryDistance

                                        kotlinx.coroutines.CoroutineScope(
                                            kotlinx.coroutines.Dispatchers.Main
                                        ).launch {

                                            try {

                                                CartData.calculatingRoute = true

                                                val routeResult =
                                                    RoadDistanceRepository.getRoadDistance(
                                                        restaurantLat,
                                                        restaurantLng,
                                                        addressData.latitude,
                                                        addressData.longitude
                                                    )

                                                val freshDistance =
                                                    routeResult.first

                                                val freshDeliveryTime =
                                                    routeResult.second

                                                CartData.selectedDistanceKm =
                                                    freshDistance

                                                CartData.deliveryTime =
                                                    freshDeliveryTime

                                                CartData.calculatingRoute =
                                                    false

                                                if (freshDistance <= 0.0) {

                                                    isPlacingOrder = false
                                                    unavailableMessage =
                                                        "Unable to calculate the delivery distance. Please try again."
                                                    showUnavailableDialog = true

                                                    return@launch
                                                }

                                                if (
                                                    freshDistance >
                                                    freshMaxDistance
                                                ) {

                                                    isPlacingOrder = false

                                                    unavailableMessage =
                                                        "This address is %.1f KM away.\n\nRestaurant delivers only within %.1f KM. Please select a nearer address."
                                                            .format(
                                                                freshDistance,
                                                                freshMaxDistance
                                                            )

                                                    showUnavailableDialog = true
                                                    return@launch
                                                }

                                                var freeKm = 0

                                                freeDeliveryRules.forEach { rule ->

                                                    val minOrder =
                                                        (rule["minOrder"] as? Number)
                                                            ?.toInt() ?: 0

                                                    val km =
                                                        (rule["freeKm"] as? Number)
                                                            ?.toInt() ?: 0

                                                    if (
                                                        itemTotal >= minOrder &&
                                                        km > freeKm
                                                    ) {
                                                        freeKm = km
                                                    }
                                                }

                                                val extraKm =
                                                    (freshDistance - freeKm)
                                                        .coerceAtLeast(0.0)

                                                val freshDeliveryCharge =
                                                    ceil(
                                                        extraKm *
                                                                deliveryPerKm
                                                    ).toInt()

                                                deliveryCharge =
                                                    freshDeliveryCharge

                                                CartData.deliveryFee.value =
                                                    freshDeliveryCharge

                                                db.collection("restaurants")
                                                    .document(restaurantId)
                                                    .collection("menu")
                                                    .get()
                                                    .addOnSuccessListener { result ->

                                                        val hiddenItems =
                                                            mutableListOf<String>()

                                                        val outOfStockItems =
                                                            mutableListOf<String>()

                                                        val removedItems =
                                                            mutableListOf<String>()

                                                        CartData.items.forEach { cartItem ->

                                                            val menuDoc =
                                                                result.documents
                                                                    .firstOrNull {
                                                                        it.getString("name")
                                                                            ?.trim()
                                                                            ?.equals(
                                                                                cartItem.item.name.trim(),
                                                                                ignoreCase = true
                                                                            ) == true
                                                                    }

                                                            when {

                                                                menuDoc == null -> {
                                                                    removedItems.add(
                                                                        cartItem.item.name
                                                                    )
                                                                }

                                                                menuDoc.getBoolean("visible")
                                                                        == false -> {
                                                                    hiddenItems.add(
                                                                        cartItem.item.name
                                                                    )
                                                                }

                                                                menuDoc.getBoolean("available")
                                                                        == false -> {
                                                                    outOfStockItems.add(
                                                                        cartItem.item.name
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        val problems =
                                                            mutableListOf<String>()

                                                        if (outOfStockItems.isNotEmpty()) {
                                                            problems.add(
                                                                "Out of stock:\n• " +
                                                                        outOfStockItems
                                                                            .distinct()
                                                                            .joinToString("\n• ")
                                                            )
                                                        }

                                                        if (hiddenItems.isNotEmpty()) {
                                                            problems.add(
                                                                "No longer available:\n• " +
                                                                        hiddenItems
                                                                            .distinct()
                                                                            .joinToString("\n• ")
                                                            )
                                                        }

                                                        if (removedItems.isNotEmpty()) {
                                                            problems.add(
                                                                "Removed from the menu:\n• " +
                                                                        removedItems
                                                                            .distinct()
                                                                            .joinToString("\n• ")
                                                            )
                                                        }

                                                        if (problems.isNotEmpty()) {

                                                            isPlacingOrder = false

                                                            unavailableMessage =
                                                                problems.joinToString(
                                                                    separator = "\n\n"
                                                                ) +
                                                                        "\n\nPlease remove these items from your cart."

                                                            showUnavailableDialog = true
                                                            return@addOnSuccessListener
                                                        }

                                                        isPlacingOrder = false
                                                        navController.navigate("payment")
                                                    }
                                                    .addOnFailureListener {

                                                        isPlacingOrder = false

                                                        Toast.makeText(
                                                            context,
                                                            "Unable to verify the menu. Please check your internet and try again.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    }

                                            } catch (error: Exception) {

                                                CartData.calculatingRoute = false
                                                isPlacingOrder = false

                                                Toast.makeText(
                                                    context,
                                                    "Unable to calculate the route. Please try again.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    }
                                    .addOnFailureListener {

                                        isPlacingOrder = false

                                        Toast.makeText(
                                            context,
                                            "Unable to verify the restaurant. Please check your internet and try again.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                            },
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor =
                                        Color(0xFFE23744)
                                ),

                            shape =
                                RoundedCornerShape(14.dp),

                            modifier =
                                Modifier.height(54.dp)
                        ) {

                            Text(

                                text =

                                    if (isPlacingOrder)

                                        "Checking Order..."

                                    else if (calculatingRoute)

                                        "Calculating Route..."

                                    else

                                        "Place Order",

                                fontWeight = FontWeight.Bold

                            )
                        }
                    }
                }
            }
        }

    ) { padding ->
        if (cartItems.isEmpty()) {

            Box(

                modifier = Modifier
                    .fillMaxSize(),

                contentAlignment =
                    Alignment.Center
            ) {

                Column(

                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "🛒",
                        fontSize = 60.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(16.dp)
                    )

                    Text(
                        text =
                            "Your cart is empty",
                        fontWeight =
                            FontWeight.Bold,
                        fontSize = 22.sp
                    )

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Button(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {

                        Text("Add Items")
                    }
                }
            }

        } else {
            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5)),

                contentPadding =
                    PaddingValues(bottom = 120.dp)
            ) {

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        shape =
                            RoundedCornerShape(20.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            cartItems.forEach { cartItem ->

                                Row(

                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp),

                                    horizontalArrangement =
                                        Arrangement.SpaceBetween
                                ) {

                                    Column(
                                        modifier =
                                            Modifier.weight(1f)
                                    ) {

                                        Text(
                                            text =
                                                cartItem.item.name,

                                            fontWeight =
                                                FontWeight.Bold,

                                            fontSize = 18.sp
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(4.dp)
                                        )

                                        if (
                                            cartItem.selectedVariant != null
                                        ) {

                                            Text(

                                                text = buildString {

                                                    append(
                                                        cartItem.selectedVariant.name
                                                    )

                                                    cartItem.selectedAddons
                                                        .forEach {

                                                            append(", ")
                                                            append(it.name)
                                                        }
                                                },

                                                color = Color.Gray,

                                                fontSize = 14.sp
                                            )
                                        }

                                        Spacer(
                                            modifier =
                                                Modifier.height(6.dp)
                                        )

                                        Spacer(
                                            modifier =
                                                Modifier.height(6.dp)
                                        )

                                    }

                                    Row(
                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        Column(
                                            horizontalAlignment =
                                                Alignment.CenterHorizontally
                                        ) {

                                            Row(
                                                verticalAlignment =
                                                    Alignment.CenterVertically
                                            ) {

                                                OutlinedButton(
                                                    onClick = {
                                                        CartData.decrease(
                                                            cartItem
                                                        )
                                                    },

                                                    contentPadding =
                                                        PaddingValues(0.dp),

                                                    modifier =
                                                        Modifier.size(34.dp)
                                                ) {

                                                    Text("-")
                                                }

                                                Text(
                                                    text =
                                                        cartItem.quantity.toString(),

                                                    modifier =
                                                        Modifier.padding(
                                                            horizontal = 12.dp
                                                        ),

                                                    fontWeight =
                                                        FontWeight.Bold
                                                )

                                                OutlinedButton(
                                                    onClick = {
                                                        CartData.increase(
                                                            cartItem
                                                        )
                                                    },

                                                    contentPadding =
                                                        PaddingValues(0.dp),

                                                    modifier =
                                                        Modifier.size(34.dp)
                                                ) {

                                                    Text("+")
                                                }
                                            }

                                            Spacer(
                                                modifier =
                                                    Modifier.height(6.dp)
                                            )

                                            Text(

                                                text =
                                                    "₹${
                                                        (
                                                                (
                                                                        cartItem.selectedVariant?.price
                                                                            ?: cartItem.item.price
                                                                        )
                                                                        +
                                                                        cartItem.selectedAddons.sumOf {
                                                                            it.price
                                                                        }
                                                                ) * cartItem.quantity
                                                    }",

                                                fontWeight =
                                                    FontWeight.SemiBold,

                                                fontSize = 15.sp,

                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(10.dp)
                            )

                            Text(

                                text = "+ Add more items",

                                color =
                                    Color(0xFFE23744),

                                fontWeight =
                                    FontWeight.Bold,

                                modifier =
                                    Modifier.clickable {

                                        val restaurantId =

                                            CartData
                                                .currentRestaurantId
                                                .value

                                        if (
                                            restaurantId.isNotEmpty()
                                        ) {

                                            navController.navigate(

                                                "restaurant_detail/$restaurantId"
                                            )
                                        }
                                    }
                            )

                            Spacer(
                                modifier = Modifier.height(18.dp)
                            )

                            OutlinedTextField(

                                value = instruction,

                                onValueChange = {
                                    instruction = it
                                },

                                modifier =
                                    Modifier.fillMaxWidth(),

                                placeholder = {

                                    Text(
                                        "Add cooking instruction"
                                    )
                                },

                                shape =
                                    RoundedCornerShape(14.dp),

                                singleLine = true
                            )
                        }
                    }
                }

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),

                        shape =
                            RoundedCornerShape(20.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color(0xFFEAF3FF)
                            )
                    ) {

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Column {

                                Text(
                                    text =
                                        "Save extra with coupons",
                                    fontWeight =
                                        FontWeight.Bold
                                )

                                Spacer(
                                    modifier =
                                        Modifier.height(4.dp)
                                )

                                Text(
                                    text =
                                        "SAVE50"
                                )
                            }

                            OutlinedButton(

                                onClick = {

                                }
                            ) {

                                Text("APPLY")
                            }
                        }
                    }
                }

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        shape =
                            RoundedCornerShape(20.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text =
                                    "Delivery in $deliveryTime",
                                fontWeight =
                                    FontWeight.Bold,
                                fontSize = 20.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(10.dp)
                            )

                            Text(
                                text =
                                    "Delivery at",
                                fontWeight =
                                    FontWeight.SemiBold
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(10.dp)
                            )

                            Card(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable {

                                            navController.navigate(
                                                "select_address?from=cart"
                                            )
                                        },

                                shape =
                                    RoundedCornerShape(14.dp),

                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            Color.White
                                    ),

                                border =
                                    BorderStroke(
                                        1.dp,
                                        Color.LightGray
                                    )
                            ) {

                                Column(

                                    modifier =
                                        Modifier.padding(16.dp)
                                ) {

                                    Text(
                                        text = address,
                                        fontSize = 16.sp
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(6.dp)
                                    )

                                    Text(

                                        text =

                                            if (calculatingRoute)

                                                "Calculating road distance..."

                                            else

                                                "Road distance: %.1f KM".format(distanceKm),

                                        color = Color.Gray,

                                        fontSize = 13.sp

                                    )
                                    Spacer(
                                        modifier = Modifier.height(4.dp)
                                    )

                                    Text(

                                        text = "ETA : $deliveryTime",

                                        color = Color.Gray,

                                        fontSize = 13.sp

                                    )
                                }
                            }
                        }
                    }
                }

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),

                        shape =
                            RoundedCornerShape(20.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Text(
                                text = "Bill Summary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )

                            Spacer(
                                modifier =
                                    Modifier.height(18.dp)
                            )

                            BillRow(
                                "Item Total",
                                itemTotal.toDouble()
                            )

                            BillExpandableRow(
                                title =
                                    "Restaurant Packaging Charges",
                                amount =
                                    packagingCharge.toDouble(),
                                expanded =
                                    deliveryExpand,
                                onClick = {
                                    deliveryExpand =
                                        !deliveryExpand
                                }
                            )

                            if (deliveryExpand) {

                                Text(
                                    text =
                                        "Packaging charges are decided by restaurant",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier =
                                        Modifier.padding(
                                            bottom = 10.dp
                                        )
                                )
                            }

                            BillExpandableRow(
                                title =
                                    "Delivery Fee",
                                amount =
                                    deliveryCharge.toDouble(),
                                expanded =
                                    false,
                                onClick = { }
                            )
                            if (surgeFee > 0) {

                                BillRow(

                                    title =

                                        if (surgeReason.isBlank())

                                            "⚡ Surge Fee"

                                        else

                                            "⚡ Surge Fee ($surgeReason)",

                                    amount = surgeFee.toDouble()

                                )
                            }

                            BillExpandableRow(
                                title =
                                    "Platform Fee",
                                amount =
                                    platformFee.toDouble(),
                                expanded =
                                    platformExpand,
                                onClick = {
                                    platformExpand =
                                        !platformExpand
                                }
                            )

                            if (platformExpand) {

                                Text(
                                    text =
                                        "This fee helps keep Veggie Go running",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier =
                                        Modifier.padding(
                                            bottom = 10.dp
                                        )
                                )
                            }

                            BillExpandableRow(
                                title =
                                    "GST (Govt Taxes)",
                                amount =
                                    gst,
                                expanded =
                                    gstExpand,
                                onClick = {
                                    gstExpand =
                                        !gstExpand
                                }
                            )


                            if (gstExpand) {

                                Column(
                                    modifier =
                                        Modifier.padding(
                                            bottom = 10.dp
                                        )
                                ) {

                                    SmallBillRow(
                                        "GST on items",
                                        gstOnItems
                                    )

                                    SmallBillRow(
                                        "GST on packaging",
                                        gstOnPackaging
                                    )

                                    SmallBillRow(
                                        "GST on platform fee",
                                        gstOnPlatform
                                    )

                                    SmallBillRow(
                                        "GST on delivery fee",
                                        gstOnDelivery
                                    )
                                }
                            }
                            if (riderTip > 0) {

                                BillRow(
                                    "Rider Tip",
                                    riderTip.toDouble()
                                )
                            }
                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Divider()

                            Spacer(
                                modifier =
                                    Modifier.height(12.dp)
                            )

                            Row(

                                modifier =
                                    Modifier.fillMaxWidth(),

                                horizontalArrangement =
                                    Arrangement.SpaceBetween
                            ) {

                                Text(
                                    text = "To Pay",
                                    fontWeight =
                                        FontWeight.Bold,
                                    fontSize = 22.sp
                                )

                                Text(
                                    text =
                                        "₹$grandTotal",
                                    fontWeight =
                                        FontWeight.Bold,
                                    fontSize = 22.sp,
                                    color =
                                        Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                item {

                    Card(

                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),

                        shape =
                            RoundedCornerShape(20.dp),

                        colors =
                            CardDefaults.cardColors(
                                containerColor =
                                    Color.White
                            )
                    ) {

                        Column(
                            modifier = Modifier.padding(18.dp)
                        ) {

                            Card(

                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),

                                shape =
                                    RoundedCornerShape(20.dp),

                                colors =
                                    CardDefaults.cardColors(
                                        containerColor =
                                            Color.White
                                    )
                            ) {

                                Column(
                                    modifier = Modifier.padding(18.dp)
                                ) {

                                    Text(
                                        text =
                                            "Tip your rider",
                                        fontWeight =
                                            FontWeight.Bold,
                                        fontSize = 18.sp
                                    )

                                    Spacer(
                                        modifier =
                                            Modifier.height(14.dp)
                                    )

                                    Row(
                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp)
                                    ) {

                                    }

                                    Spacer(
                                        modifier =
                                            Modifier.height(14.dp)
                                    )

                                    Row(

                                        modifier =
                                            Modifier.fillMaxWidth(),

                                        horizontalArrangement =
                                            Arrangement.spacedBy(10.dp),

                                        verticalAlignment =
                                            Alignment.CenterVertically
                                    ) {

                                        OutlinedTextField(

                                            value = customTip,

                                            onValueChange = {
                                                customTip = it
                                            },

                                            modifier =
                                                Modifier.weight(1f),

                                            placeholder = {

                                                Text(
                                                    "Enter tip"
                                                )
                                            },

                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(
                                                keyboardType =
                                                    KeyboardType.Number
                                            ),

                                            shape =
                                                RoundedCornerShape(14.dp)
                                        )

                                        Button(

                                            onClick = {

                                                riderTip =
                                                    customTip.toIntOrNull()
                                                        ?: 0

                                                CartData.riderTip.value =
                                                    riderTip
                                            },

                                            colors =
                                                ButtonDefaults.buttonColors(
                                                    containerColor =
                                                        Color(0xFFE23744)
                                                ),

                                            shape =
                                                RoundedCornerShape(14.dp)
                                        ) {

                                            Text("ADD")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun getRestaurantClosedReason(
    doc: DocumentSnapshot
): String? {

    val online =
        doc.getBoolean("online") ?: false

    if (!online) {
        return "Restaurant is currently offline."
    }

    val temporaryClosed =
        doc.getBoolean("temporaryClosed") ?: false

    if (temporaryClosed) {

        val reason =
            doc.getString("temporaryCloseReason")
                ?: doc.getString("closeReason")
                ?: doc.getString("temporaryClosedReason")
                ?: ""

        return if (reason.isBlank()) {
            "Restaurant is temporarily closed."
        } else {
            "Restaurant is temporarily closed.\n\nReason: $reason"
        }
    }

    val isHoliday =
        doc.getBoolean("isHoliday") ?: false

    if (isHoliday) {

        val holidayReason =
            doc.getString("holidayReason") ?: ""

        return if (holidayReason.isBlank()) {
            "Restaurant is closed for a holiday today."
        } else {
            "Restaurant is closed today.\n\nReason: $holidayReason"
        }
    }

    val weeklySlots =
        doc.get("weeklySlots") as? Map<*, *>
            ?: return "Restaurant opening schedule is not available."

    val dayName =
        SimpleDateFormat(
            "EEEE",
            Locale.ENGLISH
        ).format(java.util.Date())

    val todaySlots =
        weeklySlots[dayName] as? List<*>
            ?: emptyList<Any>()

    val now =
        java.util.Calendar.getInstance()

    val currentMinutes =
        now.get(java.util.Calendar.HOUR_OF_DAY) * 60 +
                now.get(java.util.Calendar.MINUTE)

    var nextOpeningMinutes: Int? = null

    todaySlots.forEach { rawSlot ->

        val slot =
            rawSlot as? Map<*, *>
                ?: return@forEach

        val startText =
            slot["start"]?.toString()
                ?: return@forEach

        val endText =
            slot["end"]?.toString()
                ?: return@forEach

        val startMinutes =
            parseTimeToMinutes(startText)
                ?: return@forEach

        val endMinutes =
            parseTimeToMinutes(endText)
                ?: return@forEach

        val active =
            if (startMinutes <= endMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                currentMinutes >= startMinutes ||
                        currentMinutes < endMinutes
            }

        if (active) {
            return null
        }

        if (startMinutes > currentMinutes) {

            if (
                nextOpeningMinutes == null ||
                startMinutes < nextOpeningMinutes!!
            ) {
                nextOpeningMinutes = startMinutes
            }
        }
    }

    if (nextOpeningMinutes != null) {
        return "Restaurant is currently closed.\n\nOpens today at ${
            formatMinutesAsTime(nextOpeningMinutes!!)
        }."
    }

    val days =
        listOf(
            "Sunday",
            "Monday",
            "Tuesday",
            "Wednesday",
            "Thursday",
            "Friday",
            "Saturday"
        )

    val todayIndex =
        days.indexOf(dayName)

    for (offset in 1..7) {

        val nextDay =
            days[(todayIndex + offset) % 7]

        val slots =
            weeklySlots[nextDay] as? List<*>
                ?: continue

        val firstOpening =
            slots.mapNotNull { rawSlot ->

                val slot =
                    rawSlot as? Map<*, *>
                        ?: return@mapNotNull null

                parseTimeToMinutes(
                    slot["start"]?.toString() ?: ""
                )
            }.minOrNull()

        if (firstOpening != null) {
            return "Restaurant is currently closed.\n\nOpens $nextDay at ${
                formatMinutesAsTime(firstOpening)
            }."
        }
    }

    return "Restaurant is currently closed. No upcoming opening slot is available."
}

private fun parseTimeToMinutes(
    value: String
): Int? {

    val cleanValue =
        value.trim()

    val twentyFourHourParts =
        cleanValue.split(":")

    if (
        twentyFourHourParts.size == 2 &&
        !cleanValue.contains("AM", true) &&
        !cleanValue.contains("PM", true)
    ) {

        val hour =
            twentyFourHourParts[0].toIntOrNull()
                ?: return null

        val minute =
            twentyFourHourParts[1].toIntOrNull()
                ?: return null

        if (
            hour !in 0..23 ||
            minute !in 0..59
        ) {
            return null
        }

        return hour * 60 + minute
    }

    return try {

        val parser =
            SimpleDateFormat(
                "h:mm a",
                Locale.ENGLISH
            )

        parser.isLenient = false

        val date =
            parser.parse(
                cleanValue.uppercase(Locale.ENGLISH)
            ) ?: return null

        val calendar =
            java.util.Calendar.getInstance()

        calendar.time = date

        calendar.get(
            java.util.Calendar.HOUR_OF_DAY
        ) * 60 +
                calendar.get(
                    java.util.Calendar.MINUTE
                )

    } catch (_: Exception) {
        null
    }
}

private fun formatMinutesAsTime(
    minutes: Int
): String {

    val hour24 =
        (minutes / 60) % 24

    val minute =
        minutes % 60

    val suffix =
        if (hour24 < 12) "AM" else "PM"

    val hour12 =
        when (val value = hour24 % 12) {
            0 -> 12
            else -> value
        }

    return String.format(
        Locale.ENGLISH,
        "%d:%02d %s",
        hour12,
        minute,
        suffix
    )
}

@Composable
fun BillRow(
    title: String,
    amount: Double
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            color = Color.DarkGray
        )

        Text(
            text =
                "₹%.2f".format(
                    amount.toDouble()
                )
        )
    }
}

@Composable
fun SmallBillRow(
    title: String,
    amount: Double
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween
    ) {

        Text(
            text = title,
            color = Color.Gray,
            fontSize = 13.sp
        )

        Text(
            text =
                "₹%.2f".format(
                    amount.toDouble()
                ),
            fontSize = 13.sp
        )
    }
}

@Composable
fun BillExpandableRow(
    title: String,
    amount: Double,
    expanded: Boolean,
    onClick: () -> Unit
) {

    Row(

        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(vertical = 8.dp),

        horizontalArrangement =
            Arrangement.SpaceBetween,

        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment =
                Alignment.CenterVertically
        ) {

            Text(
                text = title,
                color = Color.DarkGray
            )

            Spacer(
                modifier =
                    Modifier.width(4.dp)
            )

            Icon(
                if (expanded)
                    Icons.Default.KeyboardArrowUp
                else
                    Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        Text(
            text =
                "₹%.2f".format(
                    amount.toDouble()
                )
        )
    }
}