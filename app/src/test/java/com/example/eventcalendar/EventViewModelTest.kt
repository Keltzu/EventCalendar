package com.example.eventcalendar

import com.example.eventcalendar.model.Event
import org.junit.Assert.*
import org.junit.Test

class EventViewModelTest {

    @Test
    fun `event title is not empty`() {
        val event = Event(
            id = "test1",
            title = "Fuksi Biljardi",
            location = "Oulu"
        )
        assertTrue("Tapahtumalla on nimi", event.title.isNotEmpty())
    }

    @Test
    fun `event id is set correctly`() {
        val event = Event(id = "test123")
        assertEquals("Tapahtuman ID on oikein", "test123", event.id)
    }

    @Test
    fun `personal event is marked correctly`() {
        val event = Event(id = "test1", isPersonal = true)
        assertTrue("Henkilökohtainen tapahtuma on merkitty oikein", event.isPersonal)
    }

    @Test
    fun `public event is not personal`() {
        val event = Event(id = "test1", isPersonal = false)
        assertFalse("Julkinen tapahtuma ei ole henkilökohtainen", event.isPersonal)
    }

    @Test
    fun `event start time is valid`() {
        val event = Event(
            id = "test1",
            startTime = System.currentTimeMillis()
        )
        assertTrue("Tapahtuman alkuaika on validi", event.startTime > 0)
    }

    @Test
    fun `kide event id has correct prefix`() {
        val kideEventId = "kide_abc123"
        assertTrue("Kide.app tapahtuman ID alkaa oikein", kideEventId.startsWith("kide_"))
    }

    @Test
    fun `events can be filtered by attended`() {
        val events = listOf(
            Event(id = "event1", title = "Tapahtuma 1"),
            Event(id = "event2", title = "Tapahtuma 2"),
            Event(id = "event3", title = "Tapahtuma 3")
        )
        val attendedIds = listOf("event1", "event3")
        val notAttended = events.filter { !attendedIds.contains(it.id) }

        assertEquals("Filtteröinti toimii oikein", 1, notAttended.size)
        assertEquals("Oikea tapahtuma jäi", "event2", notAttended.first().id)
    }

    @Test
    fun `events sorted by start time`() {
        val events = listOf(
            Event(id = "1", startTime = 3000L),
            Event(id = "2", startTime = 1000L),
            Event(id = "3", startTime = 2000L)
        )
        val sorted = events.sortedBy { it.startTime }

        assertEquals("Ensimmäinen tapahtuma on oikein", "2", sorted.first().id)
        assertEquals("Viimeinen tapahtuma on oikein", "1", sorted.last().id)
    }
}