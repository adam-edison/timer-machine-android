package xyz.aprildown.timer.domain.entities

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StepEntityKtTest {

    @Test
    fun `null condition always matches`() {
        assertTrue(null.matchesDayOfWeek(calendarDayIndex = 0))
        assertTrue(null.matchesDayOfWeek(calendarDayIndex = 6))
    }

    @Test
    fun `condition matches only its selected days`() {
        // Monday and Friday only.
        val mondayAndFriday = listOf(true, false, false, false, true, false, false)

        assertTrue(mondayAndFriday.matchesDayOfWeek(calendarDayIndex = 0))
        assertTrue(mondayAndFriday.matchesDayOfWeek(calendarDayIndex = 4))
        assertFalse(mondayAndFriday.matchesDayOfWeek(calendarDayIndex = 1))
        assertFalse(mondayAndFriday.matchesDayOfWeek(calendarDayIndex = 6))
    }
}
