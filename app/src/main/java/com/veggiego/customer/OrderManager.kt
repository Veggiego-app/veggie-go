package com.veggiego.customer

import com.google.firebase.firestore.FirebaseFirestore

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun placeOrder(

    items: List<String>,

    total: Int,

    address: String,

    paymentType: String,

    customerName: String,

    phone: String,

    context: Context,

    onSuccess: () -> Unit

) {
    val connectivityManager =

        context.getSystemService(
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

        android.widget.Toast.makeText(

            context,

            "📡 No Internet Connection",

            android.widget.Toast.LENGTH_LONG

        ).show()

        return
    }
    val db = FirebaseFirestore.getInstance()

    // ✅ UNIQUE ORDER ID

    val orderId =
        System.currentTimeMillis().toString()

    // ✅ ORDER DATA

    val order = hashMapOf(

        "orderId" to orderId,

        "customerName" to customerName,

        "phone" to phone,

        "items" to items,

        "total" to total,

        "address" to address,

        "customerLat" to 23.0752,

        "customerLng" to 70.1321,

        "status" to "PENDING", // ✅ ADMIN FIRST

        "restaurantId" to "pizza_hub",

        "restaurantName" to "Pizza Hub",

        "paymentType" to paymentType,

        "timestamp" to System.currentTimeMillis(),

        // ✅ RIDER DEFAULT DATA

        "riderName" to "",

        "riderPhone" to "",

        "riderId" to ""

    )

    // ✅ SAVE ORDER

    db.collection("orders")
        .add(order)

        .addOnSuccessListener {

            onSuccess()
        }

        .addOnFailureListener {

            it.printStackTrace()
        }
}