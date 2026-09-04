package xyz.aprildown.timer.presentation.stream

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import xyz.aprildown.timer.domain.entities.StepEntity
import xyz.aprildown.timer.domain.entities.TimerEntity

/**
 * Each case is reasoned by hand (a real day-by-day walk of the week), not derived
 * from [getMinTotalTime]/[getMaxTotalTime] themselves — a shortcut that treats a
 * condition as simply "on" or "off" everywhere gets these wrong whenever two steps'
 * conditions interact (complementary, overlapping, or leaving a gap day).
 */
@RunWith(Parameterized::class)
class TimerMinMaxTotalTimeParameterizedTest(
    private val description: String,
    private val step1Days: List<Boolean>?,
    private val step2Days: List<Boolean>?,
    private val expectedMin: Long,
    private val expectedMax: Long,
) {

    @Test
    fun `min and max match the hand-reasoned bounds`() {
        val timer = buildTwoStepTimer(step1Days, step2Days)

        assertEquals("min for: $description", expectedMin, timer.getMinTotalTime())
        assertEquals("max for: $description", expectedMax, timer.getMaxTotalTime())
    }

    @Test
    fun `min and max are the actual smallest and largest of the 7 real days`() {
        val timer = buildTwoStepTimer(step1Days, step2Days)
        val realDayTotals = (0..6).map { timer.getTotalTime(calendarDayIndex = it) }

        assertEquals("min for: $description", realDayTotals.min(), timer.getMinTotalTime())
        assertEquals("max for: $description", realDayTotals.max(), timer.getMaxTotalTime())
    }

    companion object {
        private const val STEP_LENGTH = 5_000L

        private const val MONDAY = 0
        private const val TUESDAY = 1
        private const val WEDNESDAY = 2
        private const val THURSDAY = 3
        private const val FRIDAY = 4
        private const val SATURDAY = 5
        private const val SUNDAY = 6

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun data(): Collection<Array<Any?>> {
            val monWedFri = dayList(MONDAY, WEDNESDAY, FRIDAY)
            val everyOtherDay = dayList(TUESDAY, THURSDAY, SATURDAY, SUNDAY)
            val mondayOnly = dayList(MONDAY)
            val tuesdayOnly = dayList(TUESDAY)
            val weekdays = dayList(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)
            val fridayThroughSunday = dayList(FRIDAY, SATURDAY, SUNDAY)
            val everyDayChecked = dayList(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY)
            val fridayOnly = dayList(FRIDAY)
            val noDaysChecked = dayList()

            return listOf(
                arrayOf(
                    "no conditions on either step: both run every day",
                    null, null,
                    2 * STEP_LENGTH, 2 * STEP_LENGTH
                ),
                arrayOf(
                    "complementary conditions cover every day between them: exactly one runs daily",
                    monWedFri, everyOtherDay,
                    STEP_LENGTH, STEP_LENGTH
                ),
                arrayOf(
                    "non-overlapping conditions leave a real gap day where neither runs",
                    mondayOnly, tuesdayOnly,
                    0L, STEP_LENGTH
                ),
                arrayOf(
                    "overlapping conditions: both run on the shared day (Friday)",
                    weekdays, fridayThroughSunday,
                    STEP_LENGTH, 2 * STEP_LENGTH
                ),
                arrayOf(
                    "every day checked behaves as unconditioned, not as \"could be skipped\"",
                    everyDayChecked, fridayOnly,
                    STEP_LENGTH, 2 * STEP_LENGTH
                ),
                arrayOf(
                    "no days checked means the step never runs, any day",
                    noDaysChecked, null,
                    STEP_LENGTH, STEP_LENGTH
                ),
            )
        }

        private fun dayList(vararg days: Int): List<Boolean> =
            List(7) { dayIndex -> dayIndex in days }

        private fun buildTwoStepTimer(
            step1Days: List<Boolean>?,
            step2Days: List<Boolean>?
        ): TimerEntity {
            val step1 = StepEntity.Step(
                label = "step1",
                length = STEP_LENGTH,
                conditionDays = step1Days
            )
            val step2 = StepEntity.Step(
                label = "step2",
                length = STEP_LENGTH,
                conditionDays = step2Days
            )
            return TimerEntity(
                id = TimerEntity.NULL_ID,
                name = "min-max test timer",
                loop = 1,
                steps = listOf(step1, step2),
            )
        }
    }
}
