package com.veggiego.customer

data class MenuItem(

    val name: String = "",

    val price: Int = 0,

    val veg: Boolean = true,

    val available: Boolean = true,

    val category: String = "",

    val subCategory: String = "",

    val image: String = "",

    val description: String = "",

    val variants: List<VariantModel> = emptyList(),

    // 🚀 NEW

    val addons: List<AddonModel> = emptyList(),

    val recommended: Boolean = false,

    val bestseller: Boolean = false
)