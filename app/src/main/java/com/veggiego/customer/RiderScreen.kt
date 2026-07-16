package com.veggiego.customer

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore

data class RiderOrder(

    val orderId: String = "",

    val customerName: String = "",

    val status: String = ""
)

@Composable
fun RiderScreen() {

    val context = LocalContext.current

    val riderLocationService = remember {
        RiderLocationService(context)
    }

    var trackingStarted by remember {
        mutableStateOf(false)
    }

    var orders by remember {
        mutableStateOf(listOf<RiderOrder>())
    }

    val permissionLauncher =

        rememberLauncherForActivityResult(

            contract =
                ActivityResultContracts.RequestMultiplePermissions()

        ) { permissions ->

            val granted =

                permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                    ?: false

            if (granted) {

                trackingStarted = true
            }
        }

    LaunchedEffect(Unit) {

        FirebaseFirestore
            .getInstance()
            .collection("orders")

            .addSnapshotListener { snapshot, _ ->

                val list =
                    mutableListOf<RiderOrder>()

                snapshot?.documents?.forEach { doc ->

                    val status =
                        doc.getString("status")
                            ?: ""

                    if (

                        status == "READY" ||

                        status == "ARRIVED_AT_RESTAURANT" ||

                        status == "OUT_FOR_DELIVERY" ||

                        status == "ARRIVED_AT_CUSTOMER"

                    ) {

                        list.add(

                            RiderOrder(

                                orderId = doc.id,

                                customerName =
                                    doc.getString(
                                        "customerName"
                                    ) ?: "",

                                status = status
                            )
                        )
                    }
                }

                orders = list
            }
    }

    Box(

        modifier =
            Modifier.fillMaxSize()

    ) {

        LazyColumn(

            modifier =
                Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(16.dp)

        ) {

            item {

                Card(

                    modifier =
                        Modifier.fillMaxWidth(),

                    elevation =
                        CardDefaults.cardElevation(
                            defaultElevation = 10.dp
                        )

                ) {

                    Column(

                        modifier =
                            Modifier.padding(20.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally

                    ) {

                        Text(

                            text =
                                "🚴 Rider Live Tracking",

                            fontSize = 28.sp
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        Text(

                            text =

                                if (trackingStarted)

                                    "Live GPS Active 😎"

                                else

                                    "GPS Tracking Stopped"

                        )

                        Spacer(
                            modifier =
                                Modifier.height(24.dp)
                        )

                        Button(

                            onClick = {

                                val granted =

                                    ContextCompat.checkSelfPermission(

                                        context,

                                        Manifest.permission.ACCESS_FINE_LOCATION

                                    ) == PackageManager.PERMISSION_GRANTED

                                if (granted) {

                                    trackingStarted = true

                                } else {

                                    permissionLauncher.launch(

                                        arrayOf(

                                            Manifest.permission.ACCESS_FINE_LOCATION,

                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            }

                        ) {

                            Text(
                                "Start Live Tracking"
                            )
                        }
                    }
                }

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )
            }

            items(orders) { order ->

                Card(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color.White
                        )

                ) {

                    Column(

                        modifier =
                            Modifier.padding(16.dp)

                    ) {

                        Text(

                            text =
                                "Order: ${order.orderId}"
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(

                            text =
                                "Customer: ${order.customerName}"
                        )

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )

                        Text(

                            text =
                                "Status: ${order.status}"
                        )

                        Spacer(
                            modifier =
                                Modifier.height(16.dp)
                        )

                        when (order.status) {

                            "READY" -> {

                                Button(

                                    onClick = {

                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("orders")
                                            .document(order.orderId)
                                            .update(
                                                "status",
                                                "ARRIVED_AT_RESTAURANT"
                                            )

                                        riderLocationService.start(
                                            order.orderId
                                        )
                                    },

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                Color(0xFF8E24AA)
                                        )

                                ) {

                                    Text(
                                        "Arrived At Restaurant"
                                    )
                                }
                            }

                            "ARRIVED_AT_RESTAURANT" -> {

                                Button(

                                    onClick = {

                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("orders")
                                            .document(order.orderId)
                                            .update(
                                                "status",
                                                "OUT_FOR_DELIVERY"
                                            )
                                    },

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                Color(0xFF2962FF)
                                        )

                                ) {

                                    Text(
                                        "Pickup Order"
                                    )
                                }
                            }

                            "OUT_FOR_DELIVERY" -> {

                                Button(

                                    onClick = {

                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("orders")
                                            .document(order.orderId)
                                            .update(
                                                "status",
                                                "ARRIVED_AT_CUSTOMER"
                                            )
                                    },

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                Color(0xFFFF9800)
                                        )

                                ) {

                                    Text(
                                        "Reached Customer"
                                    )
                                }
                            }

                            "ARRIVED_AT_CUSTOMER" -> {

                                Button(

                                    onClick = {

                                        FirebaseFirestore
                                            .getInstance()
                                            .collection("orders")
                                            .document(order.orderId)
                                            .update(
                                                "status",
                                                "DELIVERED"
                                            )

                                        riderLocationService.stop()
                                    },

                                    colors =
                                        ButtonDefaults.buttonColors(
                                            containerColor =
                                                Color(0xFF2E7D32)
                                        )

                                ) {

                                    Text(
                                        "Delivered"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}