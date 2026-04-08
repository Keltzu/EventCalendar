package com.example.eventcalendar.repository

import com.example.eventcalendar.model.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreakRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        const val POINTS_PER_EVENT = 10
        const val STREAK_BONUS = 5
    }

    suspend fun checkIn(userId: String, eventId: String): Result<Unit> = runCatching {
        val userRef = firestore.collection("users").document(userId)
        val userDoc = userRef.get().await()
        val user = userDoc.toObject(User::class.java) ?: error("Käyttäjää ei löydy")

        // tarkistaa onko käyttäjä kirjautunut jo tapahtumaan
        if (user.attendedEvents.contains(eventId)) {
            error("Olet jo kirjautunut tähän tapahtumaan")
        }

        val now = Calendar.getInstance()
        val lastCheckIn = Calendar.getInstance().apply {
            timeInMillis = user.lastCheckIn
        }

        // laskee streakin
        val newStreak = when {
            user.lastCheckIn == 0L -> 1
            isSameWeek(now, lastCheckIn) -> user.streak
            isConsecutiveWeek(now, lastCheckIn) -> user.streak + 1
            else -> 1
        }

        // laskee pisteet
        val streakBonus = newStreak * STREAK_BONUS
        val pointsEarned = POINTS_PER_EVENT + streakBonus

        // päivittää käyttäjän tiedot
        userRef.update(
            mapOf(
                "streak" to newStreak,
                "lastCheckIn" to now.timeInMillis,
                "points" to user.points + pointsEarned,
                "weeklyPoints" to user.weekly_points + pointsEarned,
                "attendedEvents" to user.attendedEvents + eventId
            )
        ).await()
    }

    private fun isSameWeek(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    private fun isConsecutiveWeek(current: Calendar, last: Calendar): Boolean {
        val currentWeek = current.get(Calendar.WEEK_OF_YEAR)
        val lastWeek = last.get(Calendar.WEEK_OF_YEAR)
        val currentYear = current.get(Calendar.YEAR)
        val lastYear = last.get(Calendar.YEAR)

        return (currentWeek == lastWeek + 1 && currentYear == lastYear) ||
                (currentWeek == 1 && lastWeek == 52 && currentYear == lastYear + 1)
    }
}