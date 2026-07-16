package com.veggiego.customer

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object CartData {

    val items = mutableStateListOf<CartItem>()

    // Stores the ID and Name of the restaurant whose items are in the cart
    val currentRestaurantId = mutableStateOf("")
    val currentRestaurantName = mutableStateOf("")
    var selectedAddress by mutableStateOf(
        "Rajvi Nagar, Gandhidham"
    )

    var selectedDistanceKm by mutableStateOf(0.0)

    var deliveryTime by mutableStateOf("--")

    var calculatingRoute by mutableStateOf(false)

    val riderTip =
        mutableStateOf(0)

    val packagingFee =
        mutableStateOf(0)

    val deliveryFee =
        mutableStateOf(0)

    val platformFee =
        mutableStateOf(0)

    val surgeFee =
        mutableStateOf(0)

    val surgeReason =
        mutableStateOf("")

    val gst =
        mutableStateOf(0.0)

    val gstOnItems =
        mutableStateOf(0.0)

    val gstOnPackaging =
        mutableStateOf(0.0)

    val gstOnPlatform =
        mutableStateOf(0.0)

    val gstOnDelivery =
        mutableStateOf(0.0)

    fun addToCart(

        restaurantId: String,

        restaurantName: String,

        item: MenuItem,

        selectedVariant: VariantModel? = null,

        selectedAddons: List<AddonModel> = emptyList()

    ) {

        currentRestaurantId.value =
            restaurantId

        currentRestaurantName.value =
            restaurantName

        val existing =

            items.find {

                it.item.name == item.name

                        &&

                        it.selectedVariant?.name ==

                        selectedVariant?.name

                        &&

                        it.selectedAddons ==

                        selectedAddons
            }

        if (existing != null) {

            existing.quantity++

        }

        else {

            items.add(

                CartItem(

                    item = item,

                    selectedVariant =
                        selectedVariant,

                    selectedAddons =
                        selectedAddons
                )
            )
        }
    }

    fun increase(cartItem: CartItem) {
        cartItem.quantity++
    }

    fun decrease(cartItem: CartItem) {
        if (cartItem.quantity > 0) {
            cartItem.quantity--
        }
        
        if (cartItem.quantity <= 0) {
            items.remove(cartItem)
        }

        if (items.isEmpty()) {
            currentRestaurantId.value = ""
            currentRestaurantName.value = ""
        }
    }

    fun clearCart() {

        items.clear()

        currentRestaurantId.value = ""

        currentRestaurantName.value = ""

        riderTip.value = 0

        selectedDistanceKm = 0.0

        deliveryTime = "--"

        calculatingRoute = false

        surgeFee.value = 0

        surgeReason.value = ""
    }

    fun totalPrice(): Int {

        return items.sumOf {

            val basePrice =

                it.selectedVariant?.price
                    ?: it.item.price

            val addonPrice =

                it.selectedAddons.sumOf {
                        addon ->
                    addon.price
                }

            (

                    basePrice +

                            addonPrice

                    ) * it.quantity
        }
    }

    fun totalItems(): Int {
        return items.sumOf { it.quantity }
    }
}
