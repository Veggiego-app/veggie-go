package com.veggiego.customer

data class OrderModel(

    val id: String = "",

    val items: List<String> = emptyList(),

    val total: Int = 0,

    val status: String = "",

    val timestamp: Long = 0L,

    val restaurantName: String = "",

    val restaurantId: String = "",

    // ✅ USER

    val userId: String = "",

    // ✅ DELIVERY ADDRESS

    val customerName: String = "",

    val customerPhone: String = "",

    val house: String = "",

    val area: String = "",

    val city: String = "",

    val pincode: String = "",

    val landmark: String = "",

    // ✅ PAYMENT

    val paymentMethod: String = "COD",

    // ✅ RIDER

    val riderId: String = "",

    val riderName: String = "",

    val riderPhone: String = "",

    // ✅ DELIVERY STATUS

    val deliveryStatus: String = "PENDING",

    // ✅ BILL DETAILS

    val itemTotal: Int = 0,

    val deliveryFee: Int = 0,

    val platformFee: Double = 0.0,

    val gst: Double = 0.0,

    val packagingFee: Int = 0,

    val discount: Int = 0
)