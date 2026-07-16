package com.veggiego.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RecommendedSection(

    items: List<MenuItem>

) {

    Column(

        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)

    ) {

        Text(

            text = "Recommended 😍",

            style =
                MaterialTheme.typography
                    .headlineSmall,

            fontWeight = FontWeight.Bold,

            modifier = Modifier
                .padding(horizontal = 16.dp)
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        LazyRow {

            items(items) { item ->

                MenuItemCard(

                    item = item,

                    restaurantId = "recommended",

                    restaurantName = "Recommended"
                )
            }
        }
    }
}