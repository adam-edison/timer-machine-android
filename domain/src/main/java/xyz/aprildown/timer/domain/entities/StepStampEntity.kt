package xyz.aprildown.timer.domain.entities

data class StepStampEntity(
    val id: Int,
    val timerId: Int,
    val timerName: String,
    val stepName: String,
    val timestamp: Long,
    val confirmMethod: ConfirmMethod
) {

    enum class ConfirmMethod {
        AUTO,
        MANUAL
    }

    constructor(
        timerId: Int,
        timerName: String,
        stepName: String,
        confirmMethod: ConfirmMethod
    ) : this(
        id = NEW_ID,
        timerId = timerId,
        timerName = timerName,
        stepName = stepName,
        timestamp = System.currentTimeMillis(),
        confirmMethod = confirmMethod
    )

    companion object {
        const val NEW_ID = 0
    }
}
