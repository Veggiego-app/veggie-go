package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CustomerHomeScreen(
    navController: NavController
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Button(
            onClick = {

                navController.navigate("menu/pizza_hub")

            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Veg Pizza Hub")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                navController.navigate("menu/burger_point")

            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Burger Point")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                navController.navigate("menu/dosa_plaza")

            },

            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Dosa Plaza")
        }
    }
}