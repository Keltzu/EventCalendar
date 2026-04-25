package com.example.eventcalendar

import com.example.eventcalendar.model.User
import org.junit.Assert.*
import org.junit.Test

class UserModelTest {

    @Test
    fun `user has default values`() {
        val user = User()
        assertEquals("Pisteet ovat alussa 0", 0, user.points)
        assertEquals("Streak on alussa 0", 0, user.streak)
        assertTrue("Ei käytyjä tapahtumia alussa", user.attendedEvents.isEmpty())
    }

    @Test
    fun `user display name is set correctly`() {
        val user = User(displayName = "Kalle")
        assertEquals("Nimi on oikein", "Kalle", user.displayName)
    }

    @Test
    fun `user email is set correctly`() {
        val user = User(email = "kalle@testi.com")
        assertEquals("Sähköposti on oikein", "kalle@testi.com", user.email)
    }

    @Test
    fun `user points increase correctly`() {
        val user = User(points = 100)
        val newPoints = user.points + 25
        assertEquals("Pisteet kasvavat oikein", 125, newPoints)
    }

    @Test
    fun `user streak increases correctly`() {
        val user = User(streak = 3)
        val newStreak = user.streak + 1
        assertEquals("Streak kasvaa oikein", 4, newStreak)
    }

    @Test
    fun `attended events list updates correctly`() {
        val user = User(attendedEvents = listOf("event1", "event2"))
        val updatedEvents = user.attendedEvents + "event3"
        assertEquals("Tapahtumia on oikea määrä", 3, updatedEvents.size)
        assertTrue("Uusi tapahtuma lisättiin", updatedEvents.contains("event3"))
    }

    @Test
    fun `user weekly points reset to zero initially`() {
        val user = User()
        assertEquals("Viikkopisteet ovat alussa 0", 0, user.weekly_points)
    }

    @Test
    fun `longest streak is tracked correctly`() {
        val user = User(longestStreak = 5, streak = 3)
        val newLongest = maxOf(user.streak, user.longestStreak)
        assertEquals("Pisin streak on oikein", 5, newLongest)
    }
}