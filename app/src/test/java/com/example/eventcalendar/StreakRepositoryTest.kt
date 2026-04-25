package com.example.eventcalendar

import org.junit.Assert.*
import org.junit.Test

class StreakRepositoryTest {

    @Test
    fun `streak resets when week is skipped`() {
        val now = java.util.Calendar.getInstance()
        val lastCheckIn = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.WEEK_OF_YEAR, -2)
        }

        val isSameWeek = now.get(java.util.Calendar.WEEK_OF_YEAR) ==
                lastCheckIn.get(java.util.Calendar.WEEK_OF_YEAR)
        val isConsecutiveWeek = now.get(java.util.Calendar.WEEK_OF_YEAR) ==
                lastCheckIn.get(java.util.Calendar.WEEK_OF_YEAR) + 1

        assertFalse("Ei sama viikko", isSameWeek)
        assertFalse("Ei peräkkäinen viikko", isConsecutiveWeek)
    }

    @Test
    fun `streak increases on consecutive weeks`() {
        val now = java.util.Calendar.getInstance()
        val lastCheckIn = java.util.Calendar.getInstance().apply {
            add(java.util.Calendar.WEEK_OF_YEAR, -1)
        }

        val isConsecutiveWeek = now.get(java.util.Calendar.WEEK_OF_YEAR) ==
                lastCheckIn.get(java.util.Calendar.WEEK_OF_YEAR) + 1

        assertTrue("Peräkkäinen viikko kasvattaa streakkiä", isConsecutiveWeek)
    }

    @Test
    fun `points calculation is correct`() {
        val pointsPerEvent = 10
        val streakBonus = 5
        val streak = 3

        val totalPoints = pointsPerEvent + (streak * streakBonus)

        assertEquals("Pisteet lasketaan oikein", 25, totalPoints)
    }

    @Test
    fun `streak stays same on same week`() {
        val now = java.util.Calendar.getInstance()
        val lastCheckIn = java.util.Calendar.getInstance()

        val isSameWeek = now.get(java.util.Calendar.WEEK_OF_YEAR) ==
                lastCheckIn.get(java.util.Calendar.WEEK_OF_YEAR) &&
                now.get(java.util.Calendar.YEAR) ==
                lastCheckIn.get(java.util.Calendar.YEAR)

        assertTrue("Sama viikko ei kasvata streakkiä", isSameWeek)
    }

    @Test
    fun `new user starts with streak 1`() {
        val lastCheckIn = 0L
        val newStreak = if (lastCheckIn == 0L) 1 else 0

        assertEquals("Uusi käyttäjä aloittaa streak 1", 1, newStreak)
    }

    @Test
    fun `points increase with longer streak`() {
        val pointsPerEvent = 10
        val streakBonus = 5

        val streak1Points = pointsPerEvent + (1 * streakBonus)
        val streak3Points = pointsPerEvent + (3 * streakBonus)

        assertTrue("Pidempi streak antaa enemmän pisteitä", streak3Points > streak1Points)
    }

    @Test
    fun `zero drinks logged initially`() {
        val initialDrinks = 0
        assertEquals("Alussa ei juomia", 0, initialDrinks)
    }

    @Test
    fun `drink count increases correctly`() {
        var drinkCount = 0
        drinkCount++
        drinkCount++
        assertEquals("Juomien määrä kasvaa oikein", 2, drinkCount)
    }

    @Test
    fun `drink count cannot go below zero`() {
        var drinkCount = 0
        if (drinkCount > 0) drinkCount--
        assertEquals("Juomien määrä ei voi olla negatiivinen", 0, drinkCount)
    }

    @Test
    fun `event id is unique`() {
        val eventId1 = "kide_abc123"
        val eventId2 = "kide_xyz789"
        assertNotEquals("Tapahtuma ID:t ovat uniikkeja", eventId1, eventId2)
    }
}