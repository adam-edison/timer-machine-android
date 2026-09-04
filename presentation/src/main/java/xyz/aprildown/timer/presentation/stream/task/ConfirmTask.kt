package xyz.aprildown.timer.presentation.stream.task

import com.github.cardinalby.accuratecountdowntimer.AccurateCountDownTimer
import com.github.deweyreed.tools.helper.HandlerHelper

/**
 * Counts down [countDownTime] like [CountDownTimerTask]. Once it reaches zero, instead of
 * finishing (which would advance to the next step), it switches into an indefinite
 * confirm-wait phase like [StopwatchTask], calling [onConfirmTick] once a second from that
 * point (starting at 0) instead of the regular per-second [TickListener]s.
 * You must call [TaskManager.interfere] to move on from the confirm-wait phase.
 */
internal class ConfirmTask(
    master: TaskMaster,
    countDownTime: Long,
    private val onConfirmTick: (elapsedMillis: Long) -> Unit,
) : Task(master) {

    private var isConfirming = false

    private var millisLeft = countDownTime
    private var confirmMillisPassedBase = 0L
    private var confirmMillisPassedCurrent = 0L

    private var timer: AccurateCountDownTimer = CountDownPhaseTimer(millisLeft)

    override val currentTime: Long
        get() = if (isConfirming) confirmMillisPassedBase + confirmMillisPassedCurrent else millisLeft

    override fun start() {
        super.start()
        timer.start()
    }

    override fun pause() {
        super.pause()
        timer.cancel()
        timer = if (isConfirming) {
            confirmMillisPassedBase += confirmMillisPassedCurrent
            confirmMillisPassedCurrent = 0L
            ConfirmPhaseTimer()
        } else {
            CountDownPhaseTimer(millisLeft)
        }
    }

    override fun forceStop() {
        super.forceStop()
        timer.cancel()
    }

    override fun adjust(amount: Long, add: Boolean) {
        if (isConfirming) return
        timer.cancel()
        millisLeft = if (add) millisLeft + amount else amount
        timer = CountDownPhaseTimer(millisLeft)
        if (taskState.isRunning) {
            timer.start()
        }
    }

    private fun onCountDownTick(millisUntilFinished: Long) {
        millisLeft = millisUntilFinished
        master.onTick(this, currentTime)
        tick()
    }

    private fun beginConfirming() {
        isConfirming = true
        confirmMillisPassedBase = 0L
        confirmMillisPassedCurrent = 0L
        timer = ConfirmPhaseTimer()
        timer.start()
    }

    private fun onConfirmPhaseTick(millisPassed: Long) {
        confirmMillisPassedCurrent = millisPassed
        master.onTick(this, currentTime)
        onConfirmTick(currentTime)
    }

    private inner class CountDownPhaseTimer(
        countDownTime: Long,
    ) : AccurateCountDownTimer(countDownTime, 1_000L) {

        init {
            HandlerHelper.runOnUiThread {
                this@ConfirmTask.onCountDownTick(countDownTime)
            }
        }

        override fun onFinish() {
            HandlerHelper.runOnUiThread {
                this@ConfirmTask.onCountDownTick(0L)
                this@ConfirmTask.beginConfirming()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            HandlerHelper.runOnUiThread {
                this@ConfirmTask.onCountDownTick(millisUntilFinished.round())
            }
        }
    }

    private inner class ConfirmPhaseTimer : AccurateCountDownTimer(CONFIRM_DURATION, 1_000L) {

        init {
            HandlerHelper.runOnUiThread {
                this@ConfirmTask.onConfirmPhaseTick(0L)
            }
        }

        override fun onFinish() {
            // Unreachable: CONFIRM_DURATION is effectively unbounded.
        }

        override fun onTick(millisUntilFinished: Long) {
            HandlerHelper.runOnUiThread {
                this@ConfirmTask.onConfirmPhaseTick((CONFIRM_DURATION - millisUntilFinished).round())
            }
        }
    }
}

private const val CONFIRM_DURATION = Long.MAX_VALUE
