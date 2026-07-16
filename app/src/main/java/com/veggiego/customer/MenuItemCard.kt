package com.veggiego.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun MenuItemCard(

    item: MenuItem,

    restaurantId: String,

    restaurantName: String

) {

    var showVariantSheet by remember {

        mutableStateOf(false)
    }

    val hasVariants =

        item.variants.isNotEmpty()

    val normalCartItem =

        CartData.items.find {

            it.item.name == item.name

                    &&

                    it.selectedVariant == null
        }

    Card(

        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            ),

        shape =
            RoundedCornerShape(20.dp)

    ) {

        Column {

            AsyncImage(

                model = item.image,

                contentDescription = null,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )

            Column(

                modifier = Modifier
                    .padding(16.dp)

            ) {

                Text(

                    text = item.name,

                    fontSize = 22.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier
                        .height(6.dp)
                )

                Text(

                    text =
                        "₹${item.price}",

                    fontSize = 18.sp,

                    fontWeight =
                        FontWeight.Bold,

                    color =
                        Color(0xFF16A34A)
                )

                Spacer(
                    modifier = Modifier
                        .height(6.dp)
                )

                // ✅ SIZES AVAILABLE

                if (hasVariants) {

                    Text(

                        text =
                            "${item.variants.size} sizes available",

                        fontSize = 13.sp,

                        color = Color.Gray
                    )
                }

                Spacer(
                    modifier = Modifier
                        .height(14.dp)
                )

                // 🚀 VARIANT ITEM

                if (hasVariants) {

                    Button(

                        onClick = {

                            showVariantSheet =
                                true
                        }

                    ) {

                        Text("ADD")
                    }
                }

                // 🚀 NORMAL ITEM

                else {

                    if (

                        normalCartItem == null

                    ) {

                        Button(

                            onClick = {

                                CartData.addToCart(

                                    restaurantId =
                                        restaurantId,

                                    restaurantName =
                                        restaurantName,

                                    item = item
                                )
                            }

                        ) {

                            Text("ADD")
                        }
                    }

                    else {

                        Row(

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            Button(

                                onClick = {

                                    CartData.decrease(
                                        normalCartItem
                                    )
                                }

                            ) {

                                Text("-")
                            }

                            Text(

                                text =
                                    normalCartItem.quantity
                                        .toString(),

                                modifier = Modifier
                                    .padding(
                                        horizontal = 16.dp
                                    )
                            )

                            Button(

                                onClick = {

                                    CartData.increase(
                                        normalCartItem
                                    )
                                }

                            ) {

                                Text("+")
                            }
                        }
                    }
                }
            }
        }
    }

    // 🚀 VARIANT POPUP

    if (showVariantSheet) {

        ModalBottomSheet(

            onDismissRequest = {

                showVariantSheet =
                    false
            }

        ) {

            Column(

                modifier = Modifier
                    .padding(20.dp)

            ) {

                Text(

                    text =
                        "Choose Size",

                    fontSize = 22.sp,

                    fontWeight =
                        FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                )

                LazyColumn {

                    items(
                        item.variants
                    ) { variant ->

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    CartData.addToCart(

                                        restaurantId =
                                            restaurantId,

                                        restaurantName =
                                            restaurantName,

                                        item = item,

                                        selectedVariant =
                                            variant
                                    )

                                    showVariantSheet =
                                        false
                                }
                                .padding(
                                    vertical = 18.dp
                                ),

                            horizontalArrangement =
                                Arrangement.SpaceBetween
                        ) {

                            Text(

                                text =
                                    variant.name,

                                fontSize = 18.sp
                            )

                            Text(

                                text =
                                    "₹${variant.price}",

                                fontSize = 18.sp,

                                fontWeight =
                                    FontWeight.Bold,

                                color =
                                    Color(0xFF16A34A)
                            )
                        }

                        HorizontalDivider()
                    }
                }
            }
        }
    }
}