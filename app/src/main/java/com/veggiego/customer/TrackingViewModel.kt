package com.veggiego.customer

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TrackingViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _riderLocation = MutableStateFlow(LatLng(23.0758, 70.1337))
    val riderLocation: StateFlow<LatLng> = _riderLocation

    fun startTracking(orderId: String) {

        db.collection("live_locations")
            .document(orderId)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot != null && snapshot.exists()) {

                    val lat = snapshot.getDouble("lat") ?: return@addSnapshotListener
                    val lng = snapshot.getDouble("lng") ?: return@addSnapshotListener

                    _riderLocation.value = LatLng(lat, lng)
                }
            }
    }
}