package com.veggiego.customer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.PolyUtil
import com.google.maps.android.compose.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.*
import androidx.compose.foundation.BorderStroke

@Composable
fun TrackingScreen(

    navController: NavController,

    orderId: String

) {

    val context = LocalContext.current

    var riderName by remember {
        mutableStateOf("")
    }

    var riderPhone by remember {
        mutableStateOf("")
    }

    var riderId by remember {
        mutableStateOf("")
    }

    var restaurantId by remember {
        mutableStateOf("")
    }

    var orderStatus by remember {
        mutableStateOf("ACCEPTED")
    }

    var etaText by remember {
        mutableStateOf("Calculating...")
    }

    var distanceText by remember {
        mutableStateOf("0 KM")
    }

    var customerAddress by remember {
        mutableStateOf("VeggieGo Customer Address")
    }

    var restaurantLocation by remember {

        mutableStateOf(
            LatLng(23.0800, 70.1280)
        )
    }

    var customerLocation by remember {

        mutableStateOf(
            LatLng(23.0700, 70.1400)
        )
    }

    var riderLocation by remember {

        mutableStateOf(
            restaurantLocation
        )
    }

    var animatedRiderLocation by remember {

        mutableStateOf(
            riderLocation
        )
    }
    var bikeRotation by remember {

        mutableFloatStateOf(0f)
    }

    var routePoints by remember {

        mutableStateOf<List<LatLng>>(
            emptyList()
        )
    }

    var sheetExpanded by remember {

        mutableStateOf(false)
    }

    val infiniteTransition =
        rememberInfiniteTransition()

    val pulse by infiniteTransition.animateFloat(

        initialValue = 0.8f,

        targetValue = 1.15f,

        animationSpec = infiniteRepeatable(

            animation = tween(1000),

            repeatMode =
                RepeatMode.Reverse
        )
    )

    // ✅ ORDER LISTENER

    LaunchedEffect(orderId) {

        FirebaseFirestore
            .getInstance()
            .collection("orders")
            .document(orderId)

            .addSnapshotListener { value, error ->

                if (error != null) {
                    return@addSnapshotListener
                }

                if (
                    value != null &&
                    value.exists()
                ) {

                    orderStatus =

                        value.getString(
                            "deliveryStatus"
                        )

                            ?:

                                    value.getString(
                                        "status"
                                    )

                                    ?:

                                    "ACCEPTED"

                    riderName =
                        value.getString(
                            "riderName"
                        ) ?: ""

                    riderPhone =
                        value.getString(
                            "riderPhone"
                        ) ?: ""

                    riderId =
                        value.getString(
                            "riderId"
                        ) ?: ""

                    restaurantId =
                        value.getString(
                            "restaurantId"
                        ) ?: ""

                    val customerLat =

                        value.getDouble(
                            "customerLat"
                        ) ?: 23.0700

                    val customerLng =

                        value.getDouble(
                            "customerLng"
                        ) ?: 70.1400

                    customerLocation =

                        LatLng(
                            customerLat,
                            customerLng
                        )

                    customerAddress =

                        value.getString(
                            "address"
                        ) ?: "Customer Address"

                    if (
                        orderStatus ==
                        "DELIVERED"
                    ) {

                        Toast.makeText(

                            context,

                            "🎉 Order Delivered",

                            Toast.LENGTH_LONG

                        ).show()

                        navController.navigate(
                            "home"
                        ) {

                            popUpTo(0)
                        }
                    }
                }
            }
    }

    // ✅ RESTAURANT

    LaunchedEffect(restaurantId) {

        if (
            restaurantId.isNotEmpty()
        ) {

            FirebaseFirestore
                .getInstance()
                .collection("restaurants")
                .document(restaurantId)
                .get()

                .addOnSuccessListener { doc ->

                    val lat =
                        doc.getDouble("lat")
                            ?: 23.0800

                    val lng =
                        doc.getDouble("lng")
                            ?: 70.1280

                    restaurantLocation =
                        LatLng(lat, lng)

                    if (
                        orderStatus ==
                        "ACCEPTED" ||

                        orderStatus ==
                        "PREPARING" ||

                        orderStatus ==
                        "READY"
                    ) {

                        riderLocation =
                            restaurantLocation
                    }
                }
        }
    }

    // ✅ RIDER LIVE

    LaunchedEffect(riderId) {

        if (
            riderId.isNotEmpty()
        ) {

            FirebaseFirestore
                .getInstance()
                .collection("riders")
                .document(riderId)

                .addSnapshotListener { value, _ ->

                    if (value != null) {

                        val lat =

                            value.getDouble("lat")

                                ?:

                                restaurantLocation.latitude

                        val lng =

                            value.getDouble("lng")

                                ?:

                                restaurantLocation.longitude

                        val newLocation =
                            LatLng(lat, lng)

                        bikeRotation = getBearing(

                            animatedRiderLocation,

                            newLocation
                        )

                        riderLocation = newLocation
                        animatedRiderLocation = newLocation

                        val distance =

                            calculateDistance(

                                riderLocation,

                                customerLocation
                            )

                        distanceText =
                            "%.1f KM"
                                .format(distance)

                        val eta =
                            (distance * 4).toInt()

                        etaText =

                            if (eta <= 1)

                                "Arriving Now 🚀"

                            else

                                "$eta mins away"

                        val origin =

                            "${riderLocation.latitude}," +
                                    "${riderLocation.longitude}"

                        val destination =

                            "${customerLocation.latitude}," +
                                    "${customerLocation.longitude}"

                        RetrofitClient.api
                            .getDirections(

                                origin,

                                destination,

                                "AIzaSyBZxLPwh3xhYkpL1y7rk4iCGrz1Rxf6H2k"
                            )

                            .enqueue(

                                object :
                                    Callback<DirectionResponse> {

                                    override fun onResponse(

                                        call: Call<DirectionResponse>,

                                        response: Response<DirectionResponse>

                                    ) {

                                        if (
                                            response.isSuccessful
                                        ) {

                                            val points =

                                                response.body()
                                                    ?.routes
                                                    ?.firstOrNull()
                                                    ?.overview_polyline
                                                    ?.points

                                            if (points != null) {

                                                routePoints =
                                                    PolyUtil.decode(
                                                        points
                                                    )
                                            }
                                        }
                                    }

                                    override fun onFailure(

                                        call: Call<DirectionResponse>,

                                        t: Throwable

                                    ) {
                                    }
                                }
                            )
                    }
                }
        }
    }

    val cameraPositionState =

        rememberCameraPositionState {

            position =

                CameraPosition
                    .fromLatLngZoom(

                        animatedRiderLocation,

                        16f
                    )
        }

    LaunchedEffect(

        animatedRiderLocation.latitude,

        animatedRiderLocation.longitude
    ) {
        val liveDistance =

            calculateDistance(

                animatedRiderLocation,

                customerLocation
            )
        cameraPositionState.position =

            CameraPosition
                .fromLatLngZoom(

                    animatedRiderLocation,

                    if (liveDistance < 0.4)

                        19f

                    else

                        16f
                )
    }

    val statusText = when(orderStatus) {

        "ACCEPTED" ->
            "Restaurant Accepted"

        "PREPARING" ->
            "Preparing Your Food"

        "READY" ->
            "Order Ready"

        "PICKED_UP" ->
            "Picked Up By Rider"

        "OUT_FOR_DELIVERY" ->
            "Out For Delivery"

        "DELIVERED" ->
            "Delivered"

        "CANCELLED" ->
            "Order Cancelled"

        else ->
            "Waiting For Update"
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        GoogleMap(

            modifier =
                Modifier.fillMaxSize(),

            cameraPositionState =
                cameraPositionState

        ) {

            Marker(

                state =

                    MarkerState(
                        position = animatedRiderLocation
                    ),

                title = "VeggieGo Rider",

                rotation = 0f,

                flat = true,

                icon =

                    bitmapDescriptorFromVector(

                        context,

                        R.drawable.bike
                    )
            )

            Marker(

                state =
                    MarkerState(
                        position = customerLocation
                    ),

                title = "🏠 You"
            )

            if (
                routePoints.isNotEmpty()
            ) {

                Polyline(

                    points = routePoints,

                    width = 18f,

                    color = Color(
                        0xFF00C853
                    )
                )
            }
        }

        // ✅ TOP ETA CARD

        Card(
            border =

                if (

                    etaText ==
                    "Arriving Now 🚀"

                )

                    BorderStroke(

                        2.dp,

                        Color(
                            0xFF00C853
                        )
                    )

                else null,

            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 25.dp),

            shape =
                RoundedCornerShape(24.dp),

            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 12.dp
                )

        ) {

            Column(

                modifier =
                    Modifier.padding(

                        horizontal = 40.dp,
                        vertical = 22.dp
                    ),

                horizontalAlignment =
                    Alignment.CenterHorizontally

            ) {

                Text(

                    text =

                        if (statusText == "Picked Up By Rider")

                            "🛵 Picked Up By Rider"

                        else

                            statusText,

                    fontSize = 24.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = etaText
                )
            }
        }
        FloatingActionButton(

            onClick = {

                cameraPositionState.position =

                    CameraPosition
                        .fromLatLngZoom(

                            animatedRiderLocation,

                            24f
                        )
            },

            modifier =
                Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .padding(

                        start = 18.dp,

                        bottom = 220.dp
                    ),

            containerColor =
                Color.White

        ) {

            Row(

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Text(

                    text = "📍",

                    fontSize = 20.sp
                )

                Spacer(
                    modifier =
                        Modifier.width(6.dp)
                )

                Text(

                    text = "Rider",

                    fontWeight =
                        FontWeight.Bold
                )
            }
        }
        // ✅ PREMIUM BOTTOM SHEET

        Card(

            modifier =
                Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .fillMaxWidth()
                    .fillMaxHeight(

                        if (sheetExpanded)
                            0.72f
                        else
                            0.34f
                    )

                    .pointerInput(Unit) {

                        detectVerticalDragGestures {

                                _,
                                dragAmount ->

                            if (dragAmount < -10) {

                                sheetExpanded = true
                            }

                            if (dragAmount > 10) {

                                sheetExpanded = false
                            }
                        }
                    },

            shape =
                RoundedCornerShape(

                    topStart = 32.dp,
                    topEnd = 32.dp
                ),

            elevation =
                CardDefaults.cardElevation(
                    20.dp
                )

        ) {

            Column(

                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(22.dp)
                        .verticalScroll(
                            rememberScrollState()
                        )
            ) {

                Box(

                    modifier =
                        Modifier
                            .width(60.dp)
                            .height(5.dp)
                            .align(
                                Alignment.CenterHorizontally
                            )

                            .background(

                                Color.LightGray,

                                RoundedCornerShape(
                                    100.dp
                                )
                            )
                )

                Spacer(
                    modifier =
                        Modifier.height(22.dp)
                )

                Row(

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Column(

                        modifier = Modifier.weight(1f)

                    ) {

                        Text(

                            text = if (riderName.isBlank())
                                "VeggieGo Rider"
                            else
                                riderName,

                            fontSize = 24.sp,

                            fontWeight = FontWeight.Bold

                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(

                            text = statusText,

                            color = Color.Gray,

                            fontSize = 16.sp

                        )

                    }

                    Box(

                        modifier =
                            Modifier
                                .graphicsLayer {

                                    scaleX = pulse
                                    scaleY = pulse

                                }

                                .size(16.dp)

                                .background(

                                    Color(0xFF00C853),

                                    CircleShape

                                )

                    )

                }

                Spacer(
                    modifier =
                        Modifier.height(26.dp)
                )

                Row(

                    horizontalArrangement =
                        Arrangement.spacedBy(
                            14.dp
                        )
                ) {

                    Button(

                        onClick = {

                            val intent = Intent(

                                Intent.ACTION_DIAL,

                                Uri.parse(
                                    "tel:$riderPhone"
                                )
                            )

                            context.startActivity(
                                intent
                            )
                        },

                        modifier =
                            Modifier.weight(1f),

                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    ) {

                        Icon(

                            Icons.Default.Call,

                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("Call")
                    }

                    OutlinedButton(

                        onClick = {

                            navController.navigate(

                                "chat/$orderId"
                            )
                        },

                        modifier =
                            Modifier.weight(1f),

                        shape =
                            RoundedCornerShape(
                                18.dp
                            )
                    ) {

                        Icon(

                            Icons.Default.Chat,

                            contentDescription =
                                null
                        )

                        Spacer(
                            modifier =
                                Modifier.width(8.dp)
                        )

                        Text("Chat")
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(30.dp)
                )

                Text(

                    text =
                        "Delivery Progress",

                    fontWeight =
                        FontWeight.Bold,

                    fontSize = 22.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                PremiumTimelineItem(
                    "Order Accepted",
                    true
                )

                PremiumTimelineItem(

                    "Preparing Food",

                    active =
                        orderStatus == "PREPARING" ||
                                orderStatus == "READY" ||
                                orderStatus == "PICKED_UP" ||
                                orderStatus == "OUT_FOR_DELIVERY" ||
                                orderStatus == "DELIVERED",

                    current =
                        orderStatus == "PREPARING"
                )

                PremiumTimelineItem(
                    "Picked Up",
                    orderStatus == "PICKED_UP" ||
                            orderStatus == "OUT_FOR_DELIVERY" ||
                            orderStatus == "DELIVERED"
                )

                PremiumTimelineItem(

                    "Out For Delivery",

                    active =
                        orderStatus == "OUT_FOR_DELIVERY" ||
                                orderStatus == "DELIVERED",

                    current =
                        orderStatus == "OUT_FOR_DELIVERY"
                )

                PremiumTimelineItem(
                    "Delivered",
                    orderStatus == "DELIVERED"
                )

                Spacer(
                    modifier =
                        Modifier.height(30.dp)
                )

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    colors =
                        CardDefaults.cardColors(

                            containerColor =
                                Color(
                                    0xFFF1F8F4
                                )
                        ),

                    shape =
                        RoundedCornerShape(
                            22.dp
                        )
                ) {

                    Column(

                        modifier =
                            Modifier.padding(
                                18.dp
                            )
                    ) {

                        Text(

                            text =
                                "Delivery Address",

                            fontWeight =
                                FontWeight.Bold,

                            fontSize =
                                18.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(

                            text =
                                customerAddress,

                            color =
                                Color.DarkGray
                        )
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(50.dp)
                )
            }
        }
    }
}

@Composable
fun PremiumTimelineItem(

    title: String,

    active: Boolean,

    current: Boolean = false
) {

    Row(

        modifier =
            Modifier.fillMaxWidth(),

        verticalAlignment =
            Alignment.Top
    ) {

        Column(

            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {

            Box(

                modifier =
                    Modifier
                        .size(22.dp)
                        .graphicsLayer {

                            scaleX =

                                if (current)
                                    1.2f
                                else
                                    1f

                            scaleY =

                                if (current)
                                    1.2f
                                else
                                    1f
                        }

                        .background(

                            if (current)

                                Color(
                                    0xFF00E676
                                )

                            else if (active)

                                Color(
                                    0xFF00C853
                                )

                            else

                                Color.LightGray,

                            CircleShape
                        )
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Box(

                modifier =
                    Modifier
                        .width(3.dp)
                        .height(55.dp)

                        .background(

                            if (active)

                                Color(
                                    0xFF00C853
                                )

                            else

                                Color.LightGray
                        )
            )
        }

        Spacer(
            modifier =
                Modifier.width(18.dp)
        )

        Column {

            Text(

                text = title,

                fontWeight =
                    FontWeight.Bold,

                fontSize = 18.sp
            )

            Spacer(
                modifier =
                    Modifier.height(4.dp)
            )

            Text(

                text =

                    if (active)
                        "Completed"
                    else
                        "Pending",

                color =

                    if (active)

                        Color(
                            0xFF00C853
                        )

                    else

                        Color.Gray
            )
        }
    }

    Spacer(
        modifier =
            Modifier.height(12.dp)
    )
}

// ✅ DISTANCE

fun calculateDistance(

    start: LatLng,

    end: LatLng

): Double {

    val radius = 6371

    val dLat =

        Math.toRadians(
            end.latitude -
                    start.latitude
        )

    val dLng =

        Math.toRadians(
            end.longitude -
                    start.longitude
        )

    val a =

        sin(dLat / 2)
            .pow(2.0) +

                cos(
                    Math.toRadians(
                        start.latitude
                    )
                ) *

                cos(
                    Math.toRadians(
                        end.latitude
                    )
                ) *

                sin(dLng / 2)
                    .pow(2.0)

    val c =

        2 * atan2(
            sqrt(a),
            sqrt(1 - a)
        )

    return radius * c
}

// ✅ BIKE ICON

fun bitmapDescriptorFromVector(


    context: android.content.Context,

    drawableId: Int

): com.google.android.gms.maps.model.BitmapDescriptor {

    val drawable =

        ContextCompat.getDrawable(
            context,
            drawableId
        )!!

    drawable.setBounds(
        0,
        0,
        190,
        190
    )

    val bitmap =

        Bitmap.createBitmap(

            190,
            190,

            Bitmap.Config.ARGB_8888
        )

    val canvas =
        Canvas(bitmap)

    drawable.draw(canvas)

    return BitmapDescriptorFactory
        .fromBitmap(bitmap)
}
fun getBearing(

    begin: LatLng,

    end: LatLng

): Float {

    val lat =

        abs(
            begin.latitude -
                    end.latitude
        )

    val lng =

        abs(
            begin.longitude -
                    end.longitude
        )

    return when {

        begin.latitude < end.latitude &&
                begin.longitude < end.longitude ->

            Math.toDegrees(
                atan(lng / lat)
            ).toFloat()

        begin.latitude >= end.latitude &&
                begin.longitude < end.longitude ->

            (90 -
                    Math.toDegrees(
                        atan(lng / lat)
                    ) + 90).toFloat()

        begin.latitude >= end.latitude &&
                begin.longitude >= end.longitude ->

            (Math.toDegrees(
                atan(lng / lat)
            ) + 180).toFloat()

        begin.latitude < end.latitude &&
                begin.longitude >= end.longitude ->

            (90 -
                    Math.toDegrees(
                        atan(lng / lat)
                    ) + 270).toFloat()

        else -> -1f
    }
}