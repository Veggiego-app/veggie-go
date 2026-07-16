package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TrackingMapScreen(
    orderId: String,
    onBack: () -> Unit
) {

    var lat by remember { mutableStateOf(0.0) }
    var lng by remember { mutableStateOf(0.0) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .collection("tracking")
            .document(orderId)
            .addSnapshotListener { value, _ ->
                if (value != null && value.exists()) {
                    lat = value.getDouble("lat") ?: 0.0
                    lng = value.getDouble("lng") ?: 0.0
                }
            }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text("📍 Rider Location")
        Text("Lat: $lat")
        Text("Lng: $lng")

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { onBack() }) {
            Text("Back")
        }
    }
}