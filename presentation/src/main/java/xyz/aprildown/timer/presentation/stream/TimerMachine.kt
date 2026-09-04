package xyz.aprildown.timer.presentation.stream

import xyz.aprildown.timer.domain.entities.BehaviourType
import xyz.aprildown.timer.domain.entities.StepEntity
import xyz.aprildown.timer.domain.entities.TimerEntity
import xyz.aprildown.timer.domain.entities.toConfirmAction
import xyz.aprildown.timer.domain.entities.toCountAction
import xyz.aprildown.timer.domain.entities.toHalfAction
import xyz.aprildown.timer.presentation.stream.task.CountDownTimerTask
import xyz.aprildown.timer.presentation.stream.task.StopwatchTask
import xyz.aprildown.timer.presentation.stream.task.Task
import xyz.aprildown.timer.presentation.stream.task.TaskManager
import xyz.aprildown.timer.presentation.stream.task.TerminalWaitTask
import xyz.aprildown.timer.presentation.stream.task.TickListener

internal class TimerMachine(
    private val timer: TimerEntity,
    private val listener: Listener
) : TaskManager() {

    interface Listener {

        /**
         * These are similar to [TimerMachineListener]. Just to encapsulate our implementation.
         */
        fun begin(timerId: Int)

        fun started(timerId: Int, index: TimerIndex)
        fun paused(timerId: Int)
        fun updated(timerId: Int, time: Long)
        fun finished(timerId: Int)
        fun end(timerId: Int, forced: Boolean)

        fun beep()
        fun notifyHalf(halfOption: Int)
        fun countRead(content: String)

        /**
         * Called when a CONFIRM step's terminal wait begins, and again on every nag interval
         * while it continues to wait. QR_SCAN has no alert/nag of its own — see
         * [xyz.aprildown.timer.presentation.stream.task.TerminalWaitTask] usage in [toTask].
         */
        fun terminalWaitAlert(timerId: Int, index: TimerIndex)
    }

    private val timerId = timer.id
    private val theLastIndex: TimerIndex = timer.getLastIndex()

    var currentIndex: TimerIndex = timer.getFirstIndex()
        private set

    // region Custom Actions

    fun toIndex(newIndex: TimerIndex) {
        val step = timer.getStep(newIndex)
        if (step != null) {
            currentIndex = newIndex
            interfere(step.toTask())
        }
    }

    fun adjust(amount: Long) {
        currentTask?.adjust(amount, add = true)
    }

    fun to1Minute() {
        currentTask?.adjust(60_000L, add = false)
    }

    // endregion Custom Actions

    override fun onManagerBegin() {
        listener.begin(timerId)
    }

    override fun onManagerStart(newTask: Task) {
        listener.started(timerId, currentIndex)
    }

    override fun onManagerPaused(pausedTask: Task) {
        listener.paused(timerId)
    }

    override fun onManagerTick(task: Task, time: Long) {
        listener.updated(timerId, time)
    }

    override fun onManagerDone(oldTask: Task) {
        listener.finished(timerId)
    }

    override fun onManagerNoMore() {
        listener.end(timerId, forced = false)
    }

    override fun onManagerStopped() {
        listener.end(timerId, forced = true)
    }

    override fun provideFirstTask(): Task? {
        var firstIndex = timer.getFirstIndex()
        while (true) {
            val skip = timer.shouldSkip(firstIndex)
            val isLast = firstIndex == theLastIndex
            when {
                skip && isLast -> return null
                skip -> firstIndex = getNextIndexWithStep(timer.steps, timer.loop, firstIndex).first
                else -> break
            }
        }

        val (_, nextStepAfterNext) = getNextIndexWithStep(
            timer.steps,
            timer.loop,
            firstIndex
        )

        currentIndex = firstIndex
        return timer.getStep(firstIndex)?.toTask(
            useTtsNextStep = nextStepAfterNext?.behaviour?.any { it.useTts() } == true
        )
    }

    override fun provideNextTask(): Task? {
        if (currentIndex == theLastIndex) return null

        var nextIndex: TimerIndex = currentIndex
        do {
            nextIndex = getNextIndexWithStep(timer.steps, timer.loop, nextIndex).first
            val skip = timer.shouldSkip(nextIndex)
            val isLast = nextIndex == theLastIndex
            when {
                skip && isLast -> return null
                skip -> continue
                else -> break
            }
        } while (true)

        val (_, nextStepAfterNext) =
            getNextIndexWithStep(timer.steps, timer.loop, nextIndex)

        currentIndex = nextIndex
        return timer.getStep(nextIndex)?.toTask(
            useTtsNextStep = nextStepAfterNext?.behaviour?.any { it.useTts() } == true
        )
    }

    private fun StepEntity.Step.toTask(useTtsNextStep: Boolean = false): Task {
        val behaviour = behaviour
        val countUp = behaviour.find { it.type == BehaviourType.HALT } != null
        val confirmBehaviour = behaviour.find { it.type == BehaviourType.CONFIRM }
        // QR_SCAN is an orthogonal "how do you leave this step" gate (enforced separately,
        // in MachinePresenter's move guard) — it doesn't pick the task by itself except when
        // it's the *only* terminal-timing behaviour present: without HALT or CONFIRM to hold
        // the step open, a plain countdown would finish and auto-advance right past the gate.
        val qrScanBehaviour = behaviour.find { it.type == BehaviourType.QR_SCAN }

        val task = when {
            confirmBehaviour != null -> {
                val action = confirmBehaviour.toConfirmAction()
                TerminalWaitTask(
                    master = this@TimerMachine,
                    countDownTime = length,
                    onWaitTick = { elapsedMillis ->
                        val elapsedSeconds = elapsedMillis / 1000
                        val shouldAlert = elapsedSeconds == 0L ||
                            (
                                action.nagIntervalSeconds > 0 &&
                                    elapsedSeconds % action.nagIntervalSeconds == 0L
                                )
                        if (shouldAlert) {
                            listener.terminalWaitAlert(timerId, currentIndex)
                        }
                    },
                )
            }
            countUp -> StopwatchTask(master = this@TimerMachine)
            qrScanBehaviour != null -> TerminalWaitTask(
                master = this@TimerMachine,
                countDownTime = length,
                onWaitTick = {},
            )
            else -> CountDownTimerTask(master = this@TimerMachine, countDownTime = length).apply {
                if (useTtsNextStep) {
                    addTickListener(WarmUpTtsListener(warmUp = { listener.countRead("") }))
                }
            }
        }

        return task.apply {
            behaviour.forEach { item ->
                when (item.type) {
                    BehaviourType.BEEP -> {
                        addTickListener(BeepTickListener(beep = listener::beep))
                    }
                    BehaviourType.HALF -> {
                        addTickListener(
                            HalfTickListener(
                                total = length,
                                countUp = countUp,
                                half = { listener.notifyHalf(item.toHalfAction().option) },
                            )
                        )
                    }
                    BehaviourType.COUNT -> {
                        val action = item.toCountAction()
                        addTickListener(
                            CountTickListener(
                                times = action.times,
                                total = length,
                                countUp = countUp,
                                count = if (action.beep) {
                                    {
                                        if (it.isNotBlank()) {
                                            listener.beep()
                                        }
                                    }
                                } else {
                                    { listener.countRead(it) }
                                },
                            )
                        )
                    }
                    else -> Unit
                }
            }
        }
    }

    private class BeepTickListener(private val beep: () -> Unit) : TickListener {
        override fun onNewTime(newTime: Long) {
            beep()
        }
    }

    private class HalfTickListener(
        private val total: Long,
        private val countUp: Boolean = false,
        private val half: () -> Unit,
    ) : TickListener {

        private var isNotified = false

        override fun onNewTime(newTime: Long) {
            if (isNotified) return
            val isPassed = if (countUp) {
                newTime > total / 2 - 1000
            } else {
                newTime < total / 2 + 1000
            }
            if (isPassed) {
                isNotified = true
                half()
            }
        }
    }

    private class CountTickListener(
        private var times: Int,
        private val total: Long = 0L,
        private val countUp: Boolean = false,
        private val count: (String) -> Unit,
    ) : TickListener {

        private val warmUpTime = times + 1
        private var isWarmedUp = false

        override fun onNewTime(newTime: Long) {
            val remainingSeconds = if (countUp) {
                (total - newTime) / 1000
            } else {
                newTime / 1000
            }
            if (!isWarmedUp && remainingSeconds <= warmUpTime) {
                isWarmedUp = true
                count("")
            }
            if (remainingSeconds <= times && times > 0) {
                times--
                count((newTime / 1000).toString())
            }
        }
    }

    private class WarmUpTtsListener(private val warmUp: () -> Unit) : TickListener {

        private var isWarmedUp = false

        override fun onNewTime(newTime: Long) {
            if (isWarmedUp) return
            val remainingSeconds = newTime / 1000
            if (remainingSeconds <= 20) {
                warmUp()
                isWarmedUp = true
            }
        }
    }
}
