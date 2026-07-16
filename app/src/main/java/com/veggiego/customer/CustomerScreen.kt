package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CustomerScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text("🥦 VeggieGo", fontSize = 24.sp)

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            navController.navigate("payment")
        }) {
            Text("Order Now")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            navController.navigate("orders")
        }) {
            Text("My Orders")
        }
    }
}