package com.veggiego.customer

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

interface RouteApi {

    @Headers(

        "X-Goog-FieldMask: routes.distanceMeters,routes.duration"

    )

    @POST("directions/v2:computeRoutes")

    suspend fun computeRoute(

        @Body request: RouteRequest

    ): RouteResponse

    @GET("maps/api/directions/json")
    fun getDirections(

        @Query("origin")
        origin: String,

        @Query("destination")
        destination: String,

        @Query("key")
        key: String

    ): Call<DirectionResponse>

}