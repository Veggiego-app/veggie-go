package com.veggiego.customer

data class Order(

    val id: String = "",

    val items: List<String> = emptyList(),

    val total: Int = 0,

    val address: String = "",

    val status: String = "",

    val paymentType: String = "",

    val timestamp: Long = 0,

    val isReviewed: Boolean = false

)