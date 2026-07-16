package com.veggiego.customer

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val client =

        OkHttpClient.Builder()

            .connectTimeout(
                30,
                TimeUnit.SECONDS
            )

            .readTimeout(
                30,
                TimeUnit.SECONDS
            )

            .writeTimeout(
                30,
                TimeUnit.SECONDS
            )

            .addInterceptor { chain ->

                val request: Request =

                    chain.request()

                        .newBuilder()

                        .addHeader(

                            "X-Goog-Api-Key",

                            "AIzaSyBZxLPwh3xhYkpL1y7rk4iCGrz1Rxf6H2k"

                        )

                        .build()

                chain.proceed(request)

            }

            .build()

    val api: RouteApi by lazy {

        Retrofit.Builder()

            .baseUrl(
                "https://routes.googleapis.com/"
            )

            .client(client)

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(RouteApi::class.java)
    }
}