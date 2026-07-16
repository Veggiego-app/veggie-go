package com.veggiego.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

data class CartItem(

    val item: MenuItem,

    val selectedVariant: VariantModel? = null,

    val selectedAddons: List<AddonModel> = emptyList(),

    var quantityValue: Int = 1
) {

    var quantity by mutableIntStateOf(
        quantityValue
    )

    fun totalPrice(): Int {

        val basePrice =

            selectedVariant?.price
                ?: item.price

        val addonTotal =

            selectedAddons.sumOf {
                it.price
            }

        return (

                basePrice +

                        addonTotal

                ) * quantity
    }
}