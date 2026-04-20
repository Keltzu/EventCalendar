package com.example.eventcalendar.utils

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class NominatimResult(
    val lat: String = "",
    val lon: String = "",
    val display_name: String = ""
)

interface NominatimApi {
    @GET("search")
    suspend fun searchPlace(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 5,
        @Query("accept-language") language: String = "fi",
        @Query("countrycodes") countrycodes: String = "fi",
        @Query("addressdetails") addressdetails: Int = 1
    ): List<NominatimResult>
}

object NominatimService {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "EventCalendar/1.0")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://nominatim.openstreetmap.org/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: NominatimApi = retrofit.create(NominatimApi::class.java)
}