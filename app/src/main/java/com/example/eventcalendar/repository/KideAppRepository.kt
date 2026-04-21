package com.example.eventcalendar.repository

import com.example.eventcalendar.model.Event
import com.example.eventcalendar.utils.KideAppService
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KideAppRepository @Inject constructor() {

    private val cities = listOf(
        "oulu", "helsinki", "tampere", "turku",
        "jyvaskyla", "espoo", "vantaa", "rovaniemi",
        "kuopio", "lahti", "joensuu", "vaasa"
    )

    suspend fun getKideEvents(query: String = "", city: String = ""): Result<List<Event>> = runCatching {
        val citiesToFetch = if (city.isNotEmpty()) listOf(city) else cities
        val now = System.currentTimeMillis()

        val allEvents = citiesToFetch.flatMap { c ->
            try {
                val response = KideAppService.api.getEvents(query = query, city = c)
                val products = response.model ?: emptyList()
                products
                    .filter { it.productType == 1 }
                    .map { kideEvent ->
                        android.util.Log.d("KideApp", "dateStart: ${kideEvent.dateStart}")
                        Event(
                            id = "kide_${kideEvent.id}",
                            title = kideEvent.name,
                            description = kideEvent.companyName,
                            location = kideEvent.place,
                            latitude = 0.0,
                            longitude = 0.0,
                            startTime = parseDate(kideEvent.dateStart),
                            endTime = parseDate(kideEvent.dateEnd),
                            category = "Kide.app",
                            isPersonal = false,
                            createdBy = "kide"
                        )
                    }
            } catch (e: Exception) {
                emptyList()
            }
        }

        allEvents
            .distinctBy { it.id }
            .filter { it.endTime > now || it.startTime > now }
            .sortedBy { it.startTime }
    }

    private fun parseDate(dateString: String): Long {
        if (dateString.isEmpty()) return 0L
        return try {
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss"
            )
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    val date = sdf.parse(dateString)
                    if (date != null) return date.time
                } catch (e: Exception) {
                    continue
                }
            }
            0L
        } catch (e: Exception) {
            0L
        }
    }
}