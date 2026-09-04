package xyz.aprildown.timer.app.timer.list

import xyz.aprildown.timer.domain.entities.TimerEntity
import xyz.aprildown.timer.domain.entities.TimerInfo
import xyz.aprildown.timer.presentation.stream.StreamState
import xyz.aprildown.timer.presentation.timer.TimerDuration

internal data class MutableTimerItem(
    val timerInfo: TimerInfo,
    val timerDuration: TimerDuration,
    var timerItem: TimerEntity?,
    var state: StreamState,
    var isExpanded: Boolean
) {
    val timerId = timerInfo.id
    val timerName = timerInfo.name
}
