package com.veggiego.customer

// ----------------------------
// REQUEST
// ----------------------------

data class RouteRequest(

    val origin: RouteLocation,

    val destination: RouteLocation,

    val travelMode: String

)

data class RouteLocation(

    val location: LatLngLocation

)

data class LatLngLocation(

    val latLng: LatLng

)

data class LatLng(

    val latitude: Double,

    val longitude: Double

)

// ----------------------------
// RESPONSE
// ----------------------------

data class RouteResponse(

    val routes: List<Route>

)

data class Route(

    val distanceMeters: Int,

    val duration: String

)