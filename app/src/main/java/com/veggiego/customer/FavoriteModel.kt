package com.veggiego.customer

data class FavoriteRestaurant(

    val id: String = "",

    val restaurantName: String = "",

    val imageUrl: String = ""

)

data class FavoriteFood(

    val id: String = "",

    val foodName: String = "",

    val restaurantName: String = "",

    val imageUrl: String = ""

)