package com.veggiego.customer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: RouteApi by lazy {

        Retrofit.Builder()

            .baseUrl(
                "https://maps.googleapis.com/"
            )

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(RouteApi::class.java)
    }
}