package xyz.aprildown.timer.domain.entities

import java.util.Calendar

enum class StepType {
    NORMAL, NOTIFIER, START, END
}

sealed class StepEntity {
    data class Step(
        val label: String,
        val length: Long,
        val behaviour: List<BehaviourEntity> = emptyList(),
        val type: StepType = StepType.NORMAL,
        // Monday-based, same convention as SchedulerEntity.days. Null means no
        // condition: the step always runs.
        val conditionDays: List<Boolean>? = null
    ) : StepEntity()

    data class Group(
        val name: String,
        val loop: Int,
        val steps: List<StepEntity>
    ) : StepEntity()
}

/**
 * @param calendarDayIndex Monday-based day-of-week index (0 = Monday, 6 = Sunday),
 * matching [SchedulerEntity.days]. Defaults to today so production call sites don't
 * need to pass it; tests pass a fixed value to stay deterministic.
 */
fun List<Boolean>?.matchesDayOfWeek(calendarDayIndex: Int = todayCalendarDayIndex()): Boolean {
    if (this == null) return true
    return getOrElse(calendarDayIndex) { true }
}

fun todayCalendarDayIndex(): Int {
    val calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return if (calendarDay == Calendar.SUNDAY) 6 else calendarDay - Calendar.MONDAY
}
