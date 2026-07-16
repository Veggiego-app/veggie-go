package com.veggiego.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AddressCard(
    navController: NavController
) {

    val address =
        AddressData.selectedAddress.value

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable {

                navController.navigate(
                    "select_address"
                )
            },

        shape =
            RoundedCornerShape(14.dp),

        colors =
            CardDefaults.cardColors(
                containerColor =
                    Color.White
            )

    ) {

        Row(

            modifier = Modifier
                .padding(16.dp)

        ) {

            Icon(

                Icons.Default.LocationOn,

                contentDescription = null,

                tint =
                    Color(0xFF2E7D32)
            )

            Spacer(
                modifier =
                    Modifier.width(12.dp)
            )

            Column {

                Text(

                    text =
                        if (address == null)
                            "Select Delivery Address"
                        else
                            address.fullName,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(

                    text =

                        if (address == null)

                            "Tap to select address"

                        else

                            "${address.house}, ${address.area}, ${address.city}"

                )
            }
        }
    }
}