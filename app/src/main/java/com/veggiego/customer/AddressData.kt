package com.veggiego.customer

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

object AddressData {

    val selectedAddress =

        mutableStateOf<Address?>(
            null
        )

    var selectedLatitude by mutableStateOf(0.0)

    var selectedLongitude by mutableStateOf(0.0)

    var selectedArea by mutableStateOf("")

    var selectedCity by mutableStateOf("")

    var selectedPincode by mutableStateOf("")

    var mapSelected by mutableStateOf(false)
}