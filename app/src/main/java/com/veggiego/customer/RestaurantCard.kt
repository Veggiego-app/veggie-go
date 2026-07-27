package com.veggiego.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun RestaurantCard(

    restaurant: RestaurantData,

    onRestaurantClick: (String) -> Unit


) {
    val coroutineScope =
        rememberCoroutineScope()


    var isFavorite by remember(
        restaurant.id
    ) {

        mutableStateOf(false)
    }

    var favoriteLoading by remember(
        restaurant.id
    ) {

        mutableStateOf(false)
    }
    LaunchedEffect(
        restaurant.id
    ) {

        isFavorite =

            FavoriteManager
                .isRestaurantFavorite(
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
                onRestaurantClick(
                    restaurant.id
                )

            },

        shape =
            RoundedCornerShape(18.dp),

        colors =
            CardDefaults.cardColors(
                containerColor = Color(0xFFE8F5E9)
            ),

        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )

    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)

        ) {

            // ✅ IMAGE

            Box(

                modifier = Modifier
                    .size(115.dp)
                    .clip(
                        RoundedCornerShape(16.dp)
                    )
                    .background(
                        Color(0xFFF3F4F6)
                    ),

                contentAlignment =
                    Alignment.Center

            ) {

                if (

                    restaurant.imageUrl.isNotEmpty()

                ) {

                    AsyncImage(

                        model = restaurant.imageUrl,

                        contentDescription = null,

                        modifier = Modifier
                            .fillMaxSize(),

                        contentScale =
                            ContentScale.Crop
                    )

                } else {

                    Text(

                        text = "🍽️",

                        fontSize = 34.sp
                    )
                }
            }

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            // ✅ CONTENT

            Column(

                modifier = Modifier.weight(1f)

            ) {

                // ✅ TOP ROW

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween,

                    verticalAlignment =
                        Alignment.Top

                ) {

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(

                            text = restaurant.name,

                            fontSize = 15.sp,

                            fontWeight = FontWeight.Bold,

                            lineHeight = 24.sp,

                            maxLines = 2,

                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(
                            modifier =
                                Modifier.height(4.dp)
                        )

                        Text(

                            text = restaurant.category,

                            color = Color.Gray,

                            fontSize = 14.sp,

                            maxLines = 1,

                            overflow =
                                TextOverflow.Ellipsis
                        )
                    }

                    // ❤️ FAVORITE ICON

                    IconButton(

                        enabled =
                            !favoriteLoading,

                        onClick = {

                            if (restaurant.id.isBlank()) {

                                return@IconButton
                            }

                            favoriteLoading = true

                            coroutineScope.launch {

                                isFavorite =

                                    FavoriteManager
                                        .toggleRestaurantFavorite(

                                            FavoriteRestaurant(

                                                id =
                                                    restaurant.id,

                                                restaurantName =
                                                    restaurant.name,

                                                imageUrl =
                                                    restaurant.imageUrl
                                            )
                                        )

                                favoriteLoading = false
                            }
                        }
                    ) {

                        Icon(

                            imageVector =

                                if (isFavorite)

                                    Icons.Filled.Favorite
                                else

                                    Icons.Outlined.FavoriteBorder,

                            contentDescription =

                                if (isFavorite)

                                    "Remove from favorites"

                                else

                                    "Add to favorites",

                            tint =

                                if (isFavorite)

                                    Color.Red
                                else

                                    Color.Gray
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // ✅ PURE VEG

                if (restaurant.isPureVeg) {

                    Surface(

                        shape =
                            RoundedCornerShape(8.dp),

                        color =
                            Color(0xFFE8F5E9)

                    ) {

                        Text(

                            text = "🟢 Pure Veg",

                            modifier = Modifier
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 4.dp
                                ),

                            color =
                                Color(0xFF2E7D32),

                            fontWeight =
                                FontWeight.SemiBold,

                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                // ✅ DELIVERY + RATING

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement = Arrangement.SpaceBetween,

                    verticalAlignment = Alignment.CenterVertically

                ) {

                    Text(

                        text = if (restaurant.distanceText.isNotBlank())
                            restaurant.distanceText
                        else
                            restaurant.deliveryTime,

                        modifier = Modifier.weight(1f),

                        color = Color.Gray,

                        fontSize = 13.sp,

                        maxLines = 1

                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Surface(

                        color = Color(0xFF16A34A),

                        shape = RoundedCornerShape(8.dp)

                    ) {

                        Text(

                            text = "⭐ ${restaurant.rating}",

                            modifier = Modifier.padding(

                                horizontal = 8.dp,

                                vertical = 4.dp

                            ),

                            color = Color.White,

                            fontWeight = FontWeight.Bold,

                            fontSize = 12.sp

                        )

                    }

                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                if (restaurant.temporaryClosed) {

                    Text(

                        text = "🔴 TEMPORARILY CLOSED",

                        color = Color.Red,

                        fontWeight = FontWeight.Bold,

                        fontSize = 13.sp
                    )
                }

                else if (restaurant.isHoliday) {

                    Text(

                        text = "🏖 HOLIDAY TODAY",

                        color = Color.Red,

                        fontWeight = FontWeight.Bold,

                        fontSize = 13.sp
                    )
                }

                else if (

                    restaurant.autoOpen &&
                    restaurant.online

                ) {

                    Text(

                        text = "🟢 OPEN NOW",

                        color = Color(0xFF16A34A),

                        fontWeight = FontWeight.Bold,

                        fontSize = 13.sp
                    )
                }

                else {

                    Text(

                        text =
                            formatOpeningText(
                                restaurant.openingText
                            ),

                        color = Color(0xFFFF9800),

                        fontWeight = FontWeight.Bold,

                        fontSize = 13.sp
                    )
                }

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

// ✅ OFFER

                if (
                    restaurant.offer.isNotEmpty()
                ) {

                    Text(

                        text = restaurant.offer,

                        color =
                            Color(0xFF4F46E5),

                        fontWeight =
                            FontWeight.Bold,

                        fontSize = 14.sp,

                        maxLines = 1,

                        overflow =
                            TextOverflow.Ellipsis
                    )
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