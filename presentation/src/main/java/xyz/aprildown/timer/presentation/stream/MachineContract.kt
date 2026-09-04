package xyz.aprildown.timer.presentation.stream

import android.content.Context
import android.net.Uri
import xyz.aprildown.timer.domain.entities.FlashlightAction
import xyz.aprildown.timer.domain.entities.TimerEntity

/**
 * (x, y) => (horizontal, vertical) index
 * |no show|          0        |          1           |          2        |        more
 * |0      |NoNotif            |SingleTimer           |ForeNotif          |ForeNotif|
 * |1      |ForeNotif          |ForeNotif             |ForeNotif          |ForeNotif|
 * |2      |ForeNotif          |ForeNotif             |ForeNotif          |ForeNotif|
 * |more   |ForeNotif          |ForeNotif             |ForeNotif          |ForeNotif|
 *
 *         show    noShow
 * begin: right     down
 * end:    left      up
 */
sealed class NotifState

internal data object NoNotif : NotifState()
internal data object SingleTimer : NotifState()
internal data object ForeNotif : NotifState()

/**
 * Has to be here since this contract use [StreamState] which is Android specific.
 */
interface MachineContract {
    /**
     * Handles [Context] related actions
     * Updates Notifications
     */
    interface View : TimerMachineListener {
        fun prepareForWork()
        fun cleanUpWorkArea()

        fun createForegroundNotif()
        fun updateForegroundNotif(
            totalTimersCount: Int,
            pausedTimersCount: Int,
            theOnlyTimerName: String? = null
        )

        fun cancelForegroundNotif()

        fun createTimerNotification(id: Int, timer: TimerEntity)
        fun cancelTimerNotification(id: Int)

        fun stopForegroundState()

        fun toForeground(id: Int = -1)

        fun playMusic(uri: Uri, loop: Boolean)
        fun stopMusic()

        fun startVibrating(pattern: LongArray, repeat: Boolean)
        fun stopVibrating()

        fun showScreen(timerItem: TimerEntity, currentStepName: String, fullScreen: Boolean)
        fun closeScreen()

        /**
         * A QR_SCAN step just started and nothing is currently watching this timer (no
         * running screen open for it) — launch one via a full-screen-intent notification,
         * the same mechanism [showScreen] uses, so the scan can actually be triggered.
         */
        fun launchQrScanScreen(timerItem: TimerEntity, currentStepName: String)
        fun closeQrScanScreen()

        // Halt is handled in the presenter

        fun beginReading(
            content: CharSequence? = null,
            contentRes: Int = 0,
            sayMore: Boolean = false,
            afterDone: (() -> Unit)? = null
        )

        fun formatDuration(duration: Long): CharSequence
        fun formatTime(time: Long): CharSequence

        fun stopReading()

        fun enableTone(tone: Int, count: Int, respectOtherSound: Boolean)
        fun playTone()
        fun disableTone()

        fun showBehaviourNotification(timer: TimerEntity, index: TimerIndex, duration: Int)
        fun dismissBehaviourNotification()

        fun toggleFlashlight(action: FlashlightAction?, duration: Long = 0L)

        fun finish()
    }

    class CurrentTimerInfo(
        val timerEntity: TimerEntity,
        val state: StreamState,
        val index: TimerIndex,
        val time: Long
    )

    interface Presenter {
        var view: View?
        var isInTheForeground: Boolean
        var currentNotifState: NotifState

        fun takeView(view: View)
        fun dropView()

        fun addListener(timerId: Int, listener: TimerMachineListener)
        fun removeListener(timerId: Int, listener: TimerMachineListener)

        fun addAllListener(listener: TimerMachineListener)
        fun removeAllListener(listener: TimerMachineListener)

        /**
         * @return null if this timer isn't running.
         */
        fun getTimerStateInfo(id: Int): CurrentTimerInfo?

        /**
         * True while the timer's currently active step carries QR_SCAN and — if it has an
         * emergency exit configured — that many seconds haven't yet passed since the step's
         * nominal end. The single source of truth for whether "Next" is currently blocked;
         * callers must not reimplement this check, since it depends on live task state
         * (elapsed time) this interface doesn't otherwise expose.
         */
        fun isCurrentStepQrLocked(timerId: Int): Boolean

        /**
         * Seconds left until "Next" starts working again without a scan, or null if
         * there's no QR_SCAN step active, no emergency exit configured for it, its
         * countdown hasn't started yet (still before the step's nominal end), or it's
         * already available.
         */
        fun secondsUntilQrScanEmergencyExit(timerId: Int): Int?

        fun startTimer(timerId: Int, index: TimerIndex? = null)
        fun pauseTimer(timerId: Int)
        fun moveTimer(timerId: Int, index: TimerIndex)
        fun decreTimer(timerId: Int)
        fun increTimer(timerId: Int)

        /**
         * Moves off a QR_SCAN step after a successful scan, bypassing the block that
         * [moveTimer]/[decreTimer]/[increTimer] apply while that step is active.
         */
        fun advancePastQrScan(timerId: Int)
        fun resetTimer(timerId: Int)
        fun adjustAmount(timerId: Int, amount: Long, goBackOnNotifier: Boolean)

        fun startAll()
        fun pauseAll(): List<Int>
        fun stopAll()

        fun scheduleStart(timerId: Int)
        fun scheduleEnd(timerId: Int)
    }

    interface PresenterProvider {
        fun getPresenter(): Presenter
    }
}
