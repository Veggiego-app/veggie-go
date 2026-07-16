package com.veggiego.customer

import com.google.firebase.firestore.FirebaseFirestore

fun assignRider(orderId: String) {

    val db = FirebaseFirestore.getInstance()

    db.collection("riders")
        .whereEqualTo("isAvailable", true)
        .limit(1)
        .get()
        .addOnSuccessListener { result ->

            if (!result.isEmpty) {

                val rider = result.documents[0]
                val riderId = rider.id

                // 🔥 Order में rider assign
                db.collection("orders")
                    .document(orderId)
                    .update("riderId", riderId)

                // 🔥 Rider busy कर दो
                db.collection("riders")
                    .document(riderId)
                    .update("isAvailable", false)
            }
        }
}