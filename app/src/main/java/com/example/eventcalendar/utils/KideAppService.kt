package com.example.eventcalendar.utils

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class KideEvent(
    @SerializedName("name") val name: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("dateActualFrom") val dateStart: String = "",
    @SerializedName("dateActualUntil") val dateEnd: String = "",
    @SerializedName("place") val place: String = "",
    @SerializedName("slug") val slug: String = "",
    @SerializedName("id") val id: String = "",
    @SerializedName("companyName") val companyName: String = "",
    @SerializedName("productType") val productType: Int = 0
)

data class KideResponse(
    @SerializedName("model") val model: List<KideEvent>? = null
)

interface KideApi {
    @GET("api/products")
    suspend fun getEvents(
        @Query("context") context: String = "event",
        @Query("city") city: String = "",
        @Query("q") query: String = ""
    ): KideResponse
}

object KideAppService {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36")
                .header("Accept", "application/json, text/plain, */*")
                .header("Accept-Language", "fi-FI,fi;q=0.9")
                .header("Origin", "https://kide.app")
                .header("Referer", "https://kide.app/")
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.kide.app/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api: KideApi = retrofit.create(KideApi::class.java)
}