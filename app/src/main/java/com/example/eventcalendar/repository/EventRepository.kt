package com.example.eventcalendar.repository

import com.example.eventcalendar.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getEventsFlow(userId: String): Flow<List<Event>> {
        val publicEventsFlow = callbackFlow {
            val listener = firestore.collection("events")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
                    trySend(events)
                }
            awaitClose { listener.remove() }
        }

        val personalEventsFlow = callbackFlow {
            val listener = firestore.collection("users")
                .document(userId)
                .collection("personalEvents")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) return@addSnapshotListener
                    val events = snapshot?.toObjects(Event::class.java) ?: emptyList()
                    trySend(events)
                }
            awaitClose { listener.remove() }
        }

        return publicEventsFlow.combine(personalEventsFlow) { public, personal ->
            (public + personal).sortedBy { it.startTime }
        }
    }

    // henkilökohtainen tapahtuma
    suspend fun addPersonalEvent(userId: String, event: Event): Result<Unit> = runCatching {
        val docRef = firestore.collection("users")
            .document(userId)
            .collection("personalEvents")
            .document()
        val eventWithId = event.copy(
            id = docRef.id,
            isPersonal = true,
            createdBy = userId
        )
        docRef.set(eventWithId).await()
    }

    // lisää tapahtuma (julkinen)
    suspend fun addEvent(event: Event): Result<Unit> = runCatching {
        val docRef = firestore.collection("events").document()
        val eventWithId = event.copy(id = docRef.id)
        docRef.set(eventWithId).await()
    }

    // päivittäminen
    suspend fun updateEvent(event: Event): Result<Unit> = runCatching {
        if (event.isPersonal) {
            firestore.collection("users")
                .document(event.createdBy)
                .collection("personalEvents")
                .document(event.id)
                .set(event)
                .await()
        } else {
            firestore.collection("events")
                .document(event.id)
                .set(event)
                .await()
        }
    }

    // tapahtuman poisto
    suspend fun deleteEvent(event: Event): Result<Unit> = runCatching {
        if (event.isPersonal) {
            firestore.collection("users")
                .document(event.createdBy)
                .collection("personalEvents")
                .document(event.id)
                .delete()
                .await()
        } else {
            firestore.collection("events")
                .document(event.id)
                .delete()
                .await()
        }
    }
}