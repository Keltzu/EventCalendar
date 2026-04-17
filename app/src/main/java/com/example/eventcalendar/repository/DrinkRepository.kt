package com.example.eventcalendar.repository

import com.example.eventcalendar.model.DrinkLog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrinkRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveDrinkLog(drinkLog: DrinkLog): Result<Unit> = runCatching {
        val docRef = if (drinkLog.id.isEmpty()) {
            firestore.collection("drinkLogs").document()
        } else {
            firestore.collection("drinkLogs").document(drinkLog.id)
        }
        docRef.set(drinkLog.copy(id = docRef.id)).await()
    }

    suspend fun getDrinkLogForEvent(userId: String, eventId: String): Result<DrinkLog?> = runCatching {
        firestore.collection("drinkLogs")
            .whereEqualTo("userId", userId)
            .whereEqualTo("eventId", eventId)
            .get()
            .await()
            .toObjects(DrinkLog::class.java)
            .firstOrNull()
    }

    suspend fun getUserDrinkLogs(userId: String): Result<List<DrinkLog>> = runCatching {
        firestore.collection("drinkLogs")
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(DrinkLog::class.java)
    }
}