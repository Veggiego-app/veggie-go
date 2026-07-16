package com.veggiego.customer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object RoadDistanceRepository {

    suspend fun getRoadDistance(

        restaurantLat: Double,
        restaurantLng: Double,

        customerLat: Double,
        customerLng: Double

    ): Pair<Double, String> {

        return withContext(Dispatchers.IO) {

            try {

                val request = RouteRequest(

                    origin = RouteLocation(

                        location = LatLngLocation(

                            latLng = LatLng(

                                latitude = restaurantLat,
                                longitude = restaurantLng

                            )

                        )

                    ),

                    destination = RouteLocation(

                        location = LatLngLocation(

                            latLng = LatLng(

                                latitude = customerLat,
                                longitude = customerLng

                            )

                        )

                    ),

                    travelMode = "DRIVE"

                )

                val response = RetrofitClient.api.computeRoute(request)

                if (response.routes.isNotEmpty()) {

                    val route = response.routes[0]

                    val distanceKm =
                        route.distanceMeters / 1000.0

                    val eta =
                        route.duration
                            .replace("s", "")
                            .toLong() / 60

                    android.util.Log.d(
                        "ROUTES_API",
                        "Distance = $distanceKm km"
                    )

                    android.util.Log.d(
                        "ROUTES_API",
                        "Duration = $eta mins"
                    )

                    Pair(

                        distanceKm,

                        "$eta mins"

                    )

                } else {

                    Pair(0.0, "--")

                }

            } catch (e: Exception) {

                e.printStackTrace()

                android.util.Log.e(
                    "ROUTES_API",
                    e.message ?: "Unknown Error"
                )

                Pair(0.0, "--")

            }

        }

    }

}