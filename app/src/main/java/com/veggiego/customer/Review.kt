package com.veggiego.customer

data class Review(

    val restaurantId: String = "",
    val restaurantName: String = "",
    val customerName: String = "",
    val rating: Int = 0,
    val review: String = "",
    val timestamp: Long = 0

)