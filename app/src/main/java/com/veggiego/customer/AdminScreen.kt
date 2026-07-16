package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

data class AdminOrderModel(

    val orderId: String = "",

    val items: List<String> = emptyList(),

    val total: Int = 0,

    val status: String = ""
)

@Composable
fun AdminScreen() {

    var orders by remember {
        mutableStateOf(listOf<AdminOrderModel>())
    }

    LaunchedEffect(Unit) {

        FirebaseFirestore.getInstance()

            .collection("orders")

            .addSnapshotListener { snapshot, _ ->

                val list =
                    mutableListOf<AdminOrderModel>()

                snapshot?.documents?.forEach { doc ->

                    val itemsData =
                        doc.get("items") as? List<HashMap<String, Any>>

                    val itemNames =
                        itemsData?.map {

                            it["name"].toString()
                        } ?: emptyList()

                    list.add(

                        AdminOrderModel(

                            orderId = doc.id,

                            items = itemNames,

                            total =
                                doc.getLong("total")
                                    ?.toInt() ?: 0,

                            status =
                                doc.getString("status")
                                    ?: "PENDING"
                        )
                    )
                }

                orders = list.reversed()
            }
    }

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)

    ) {

        Text(

            text = "VeggieGo Admin",

            fontSize = 28.sp,

            fontWeight = FontWeight.Bold,

            color = Color(0xFF2E7D32)
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        LazyColumn {

            items(orders) { order ->

                Card(

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),

                    shape =
                        RoundedCornerShape(20.dp),

                    colors =
                        CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        )
                ) {

                    Column(

                        modifier =
                            Modifier.padding(16.dp)

                    ) {

                        Text(

                            text =
                                "Order ID: ${order.orderId}",

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(

                            text =
                                "Items: ${
                                    order.items.joinToString()
                                }"
                        )

                        Spacer(
                            modifier = Modifier.height(8.dp)
                        )

                        Text(
                            text = "₹${order.total}"
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Text(

                            text =
                                "Current Status: ${order.status}",

                            color = Color(0xFF2E7D32),

                            fontWeight =
                                FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(16.dp)
                        )

                        if (order.status == "PENDING") {

                            Button(

                                onClick = {

                                    updateStatus(
                                        order.orderId,
                                        "APPROVED"
                                    )
                                },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF43A047)
                                )

                            ) {

                                Text("Approve COD")
                            }

                        }

                        else if (

                            order.status == "APPROVED"

                        ) {

                            Button(

                                onClick = {

                                    updateStatus(
                                        order.orderId,
                                        "ACCEPTED"
                                    )
                                },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF43A047)
                                )

                            ) {

                                Text("Accept Order")
                            }
                        }

                        else if (

                            order.status == "ACCEPTED"

                        ) {

                            Button(

                                onClick = {

                                    updateStatus(
                                        order.orderId,
                                        "PREPARING"
                                    )
                                },

                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                )

                            ) {

                                Text("Start Preparing")
                            }
                        }

                        else {

                            Row(

                                horizontalArrangement =
                                    Arrangement.spacedBy(8.dp)

                            ) {

                                Button(

                                    onClick = {

                                        updateStatus(
                                            order.orderId,
                                            "READY"
                                        )
                                    },

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E88E5)
                                    )

                                ) {

                                    Text("Ready")
                                }

                                Button(

                                    onClick = {

                                        updateStatus(
                                            order.orderId,
                                            "OUT_FOR_DELIVERY"
                                        )
                                    },

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2962FF)
                                    )

                                ) {

                                    Text("Out")
                                }

                                Button(

                                    onClick = {

                                        updateStatus(
                                            order.orderId,
                                            "DELIVERED"
                                        )
                                    },

                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2E7D32)
                                    )

                                ) {

                                    Text("Done")
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

fun updateStatus(

    orderId: String,

    status: String
) {

    FirebaseFirestore.getInstance()

        .collection("orders")

        .document(orderId)

        .update("status", status)
}