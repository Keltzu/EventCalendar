package com.example.eventcalendar.repository

import com.example.eventcalendar.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun getEvents(): Result<List<Event>> = runCatching {
        firestore.collection("events")
            .get()
            .await()
            .toObjects(Event::class.java)
    }

    suspend fun addEvent(event: Event): Result<Unit> = runCatching {
        val docRef = firestore.collection("events").document()
        val eventWithId = event.copy(id = docRef.id)
        docRef.set(eventWithId).await()
    }

    suspend fun getEventById(eventId: String): Result<Event> = runCatching {
        firestore.collection("events")
            .document(eventId)
            .get()
            .await()
            .toObject(Event::class.java) ?: error("Tapahtumaa ei löydy")
    }
}