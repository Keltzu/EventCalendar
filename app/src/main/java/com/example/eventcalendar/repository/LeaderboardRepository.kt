package com.example.eventcalendar.repository

import com.example.eventcalendar.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getTopUsers(): Result<List<User>> = runCatching {
        firestore.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
            .toObjects(User::class.java)
    }

    suspend fun getWeeklyTopUsers(): Result<List<User>> = runCatching {
        firestore.collection("users")
            .orderBy("weeklyPoints", Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .await()
            .toObjects(User::class.java)
    }
}