package com.veggiego.customer

import com.google.firebase.firestore.FirebaseFirestore

class TrackingRepository {

    private val db = FirebaseFirestore.getInstance()

    fun listenLocation(orderId: String, onUpdate: (Double, Double) -> Unit) {

        db.collection("live_locations")
            .document(orderId)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null && snapshot.exists()) {
                    val lat = snapshot.getDouble("lat") ?: return@addSnapshotListener
                    val lng = snapshot.getDouble("lng") ?: return@addSnapshotListener

                    onUpdate(lat, lng)
                }
            }
    }
}