package com.veggiego.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@Composable
fun RestaurantCard(
    restaurant: RestaurantData,
    timeVersion: Long = 0L,
    onRestaurantClick: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val restaurantOpen = remember(restaurant, timeVersion) {
        isRestaurantOpenNow(restaurant)
    }

    val distanceParts = remember(
        restaurant.distanceText,
        restaurant.deliveryTime
    ) {
        restaurant.distanceText
            .split("•")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    val distanceLabel = remember(distanceParts) {
        distanceParts.firstOrNull {
            it.contains("km", ignoreCase = true)
        }.orEmpty()
    }

    val timeLabel = remember(
        distanceParts,
        restaurant.deliveryTime
    ) {
        distanceParts.firstOrNull {
            it.contains("min", ignoreCase = true)
        }.orEmpty().ifBlank {
            restaurant.deliveryTime.trim()
        }
    }

    val statusText: String
    val statusColor: Color

    when {
        restaurant.temporaryClosed -> {
            statusText = "🔴 TEMPORARILY CLOSED"
            statusColor = Color.Red
        }

        restaurant.isHoliday -> {
            statusText = "🏖 HOLIDAY TODAY"
            statusColor = Color.Red
        }

        restaurantOpen -> {
            statusText = "🟢 OPEN NOW"
            statusColor = Color(0xFF16A34A)
        }

        else -> {
            statusText =
                "🟠 ${restaurantWeeklyOpeningText(restaurant).uppercase()}"
            statusColor = Color(0xFFFF9800)
        }
    }

    var isFavorite by remember(restaurant.id) {
        mutableStateOf(false)
    }

    var favoriteLoading by remember(restaurant.id) {
        mutableStateOf(false)
    }

    LaunchedEffect(restaurant.id) {
        isFavorite =
            FavoriteManager.isRestaurantFavorite(
                restaurant.id
            )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            )
            .clickable {
                onRestaurantClick(restaurant.id)
            },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Logo/photo and its distance/time are kept in one fixed-width column.
            Column(
                modifier = Modifier.width(118.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(118.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF3F4F6)),
                    contentAlignment = Alignment.Center
                ) {
                    if (restaurant.imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(restaurant.imageUrl)
                                .size(360, 360)
                                .crossfade(true)
                                .build(),
                            contentDescription = restaurant.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = "🍽️",
                            fontSize = 34.sp
                        )
                    }
                }

                if (distanceLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(9.dp))

                    Text(
                        text = "📍 $distanceLabel",
                        color = Color(0xFF4B5563),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (timeLabel.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "🕒 $timeLabel",
                        color = Color(0xFF4B5563),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Full available width is reserved for the name; favorite is no longer beside it.
                Text(
                    text = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                    color = Color(0xFF111827),
                    fontSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (restaurant.category.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = restaurant.category,
                        color = Color(0xFF6B7280),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (restaurant.isPureVeg) {
                    Spacer(modifier = Modifier.height(9.dp))

                    Surface(
                        shape = RoundedCornerShape(50.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            text = "🌿 Pure Veg",
                            modifier = Modifier.padding(
                                horizontal = 10.dp,
                                vertical = 5.dp
                            ),
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = statusText,
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // No offer Text and no offer spacing are created when offer is blank.
                if (restaurant.offer.isNotBlank()) {
                    Spacer(modifier = Modifier.height(9.dp))

                    Text(
                        text = restaurant.offer.trim(),
                        color = Color(0xFF4F46E5),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFF16A34A),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "⭐ ${restaurant.rating}",
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 5.dp
                            ),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(
                        enabled = !favoriteLoading,
                        modifier = Modifier.size(38.dp),
                        onClick = {
                            if (restaurant.id.isBlank()) {
                                return@IconButton
                            }

                            favoriteLoading = true

                            coroutineScope.launch {
                                try {
                                    isFavorite =
                                        FavoriteManager.toggleRestaurantFavorite(
                                            FavoriteRestaurant(
                                                id = restaurant.id,
                                                restaurantName = restaurant.name,
                                                imageUrl = restaurant.imageUrl
                                            )
                                        )
                                } finally {
                                    favoriteLoading = false
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Outlined.FavoriteBorder
                                },
                            contentDescription =
                                if (isFavorite) {
                                    "Remove from favorites"
                                } else {
                                    "Add to favorites"
                                },
                            tint =
                                if (isFavorite) {
                                    Color.Red
                                } else {
                                    Color(0xFF4B5563)
                                }
                        )
                    }
                }
            }
        }
    }
}

fun formatOpeningText(
    text: String
): String {
    if (text.isBlank()) {
        return "🔴 CURRENTLY CLOSED"
    }

    return text
        .replace("Monday 0:", "Monday 12:")
        .replace("Tuesday 0:", "Tuesday 12:")
        .replace("Wednesday 0:", "Wednesday 12:")
        .replace("Thursday 0:", "Thursday 12:")
        .replace("Friday 0:", "Friday 12:")
        .replace("Saturday 0:", "Saturday 12:")
        .replace("Sunday 0:", "Sunday 12:")
}