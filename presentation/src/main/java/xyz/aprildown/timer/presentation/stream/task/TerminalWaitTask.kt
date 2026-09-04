package xyz.aprildown.timer.presentation.stream.task

import com.github.cardinalby.accuratecountdowntimer.AccurateCountDownTimer
import com.github.deweyreed.tools.helper.HandlerHelper

/**
 * Counts down [countDownTime] like [CountDownTimerTask]. Once it reaches zero, instead of
 * finishing (which would advance to the next step), it switches into an indefinite wait
 * phase like [StopwatchTask], calling [onWaitTick] once a second from that point (starting
 * at 0) instead of the regular per-second [TickListener]s.
 * You must call [TaskManager.interfere] to move on from the wait phase — used by both
 * CONFIRM (any "Next" dismisses it) and QR_SCAN (only a successful scan dismisses it); this
 * task has no opinion on how that happens.
 */
internal class TerminalWaitTask(
    master: TaskMaster,
    countDownTime: Long,
    private val onWaitTick: (elapsedMillis: Long) -> Unit,
) : Task(master) {

    private var isWaiting = false

    private var millisLeft = countDownTime
    private var waitMillisPassedBase = 0L
    private var waitMillisPassedCurrent = 0L

    private var timer: AccurateCountDownTimer = CountDownPhaseTimer(millisLeft)

    override val currentTime: Long
        get() = if (isWaiting) waitMillisPassedBase + waitMillisPassedCurrent else millisLeft

    /**
     * Milliseconds elapsed since the wait phase began, or null while still counting down
     * (i.e. before the step's nominal end) — [currentTime] alone can't distinguish those,
     * since it counts down during the first phase and up during the second.
     */
    val elapsedSinceWaitBegan: Long?
        get() = if (isWaiting) waitMillisPassedBase + waitMillisPassedCurrent else null

    override fun start() {
        super.start()
        timer.start()
    }

    override fun pause() {
        super.pause()
        timer.cancel()
        timer = if (isWaiting) {
            waitMillisPassedBase += waitMillisPassedCurrent
            waitMillisPassedCurrent = 0L
            WaitPhaseTimer()
        } else {
            CountDownPhaseTimer(millisLeft)
        }
    }

    override fun forceStop() {
        super.forceStop()
        timer.cancel()
    }

    override fun adjust(amount: Long, add: Boolean) {
        if (isWaiting) return
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

    private fun beginWaiting() {
        isWaiting = true
        waitMillisPassedBase = 0L
        waitMillisPassedCurrent = 0L
        timer = WaitPhaseTimer()
        timer.start()
    }

    private fun onWaitPhaseTick(millisPassed: Long) {
        waitMillisPassedCurrent = millisPassed
        master.onTick(this, currentTime)
        onWaitTick(currentTime)
    }

    private inner class CountDownPhaseTimer(
        countDownTime: Long,
    ) : AccurateCountDownTimer(countDownTime, 1_000L) {

        init {
            HandlerHelper.runOnUiThread {
                this@TerminalWaitTask.onCountDownTick(countDownTime)
            }
        }

        override fun onFinish() {
            HandlerHelper.runOnUiThread {
                this@TerminalWaitTask.onCountDownTick(0L)
                this@TerminalWaitTask.beginWaiting()
            }
        }

        override fun onTick(millisUntilFinished: Long) {
            HandlerHelper.runOnUiThread {
                this@TerminalWaitTask.onCountDownTick(millisUntilFinished.round())
            }
        }
    }

    private inner class WaitPhaseTimer : AccurateCountDownTimer(WAIT_DURATION, 1_000L) {

        init {
            HandlerHelper.runOnUiThread {
                this@TerminalWaitTask.onWaitPhaseTick(0L)
            }
        }

        override fun onFinish() {
            // Unreachable: WAIT_DURATION is effectively unbounded.
        }

        override fun onTick(millisUntilFinished: Long) {
            HandlerHelper.runOnUiThread {
                this@TerminalWaitTask.onWaitPhaseTick((WAIT_DURATION - millisUntilFinished).round())
            }
        }
    }
}

private const val WAIT_DURATION = Long.MAX_VALUE
