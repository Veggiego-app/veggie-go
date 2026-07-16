package com.veggiego.customer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore

class RiderLocationService(private val context: Context) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val db = FirebaseFirestore.getInstance()

    private lateinit var locationCallback: LocationCallback

    @SuppressLint("MissingPermission")
    fun start(orderId: String) {

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location: Location = result.lastLocation ?: return

                val data = hashMapOf(
                    "lat" to location.latitude,
                    "lng" to location.longitude,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("tracking")
                    .document(orderId)
                    .set(data)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null
        )
    }

    fun stop() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}