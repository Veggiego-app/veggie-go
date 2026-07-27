package com.veggiego.customer

data class RestaurantData(

    val id: String = "",

    val name: String = "",

    val category: String = "",

    val imageUrl: String = "",

    val rating: String = "4.5",

    val deliveryTime: String = "20-30 min",

    val offer: String = "",

    val isPureVeg: Boolean = true,

    val autoOpen: Boolean = false,

    val liveStatus: String = "",

    val openingText: String = "",

    val isHoliday: Boolean = false,

    val online: Boolean = true,

    val temporaryClosed: Boolean = false,

    val lat: Double = 0.0,

    val lng: Double = 0.0,

    val distanceKm: Double = 0.0,

    val distanceText: String = "",

    val displayOrder: Int = 999999
)