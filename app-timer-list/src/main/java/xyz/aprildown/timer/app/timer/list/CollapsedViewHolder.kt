package xyz.aprildown.timer.app.timer.list

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.timer.app.base.utils.produceCompactTime
import xyz.aprildown.timer.domain.entities.FolderEntity
import xyz.aprildown.timer.app.base.R as RBase

internal class CollapsedViewHolder(
    view: View,
    callback: TimerAdapter.Callback
) : RecyclerView.ViewHolder(view) {

    private val name = view.findViewById<TextView>(R.id.textTimerName)
    private val duration = view.findViewById<TextView>(R.id.textTimerDuration)
    private val iconDurationRange = view.findViewById<ImageView>(R.id.iconTimerDurationRange)
    private val durationRange = view.findViewById<TextView>(R.id.textTimerDurationRange)
    private val start = view.findViewById<ImageButton>(R.id.imageTimerStartPause)

    init {
        start.setOnClickListener {
            callback.onTimerAction(this, TimerAdapter.ACTION_COLLAPSED_START)
        }
        view.findViewById<View>(R.id.cardTimer).setOnCreateContextMenuListener { menu, _, _ ->
            callback.onTimerAction(this, TimerAdapter.ACTION_CONTEXT_MENU)
            menu?.run {
                add(0, MENU_ID_EDIT, MENU_ID_EDIT, RBase.string.edit)
                add(0, MENU_ID_DUPLICATE, MENU_ID_DUPLICATE, RBase.string.duplicate)
                add(0, MENU_ID_MOVE, MENU_ID_MOVE, RBase.string.move)
                add(0, MENU_ID_DELETE, MENU_ID_DELETE, RBase.string.delete)
                add(0, MENU_ID_SHARE, MENU_ID_SHARE, RBase.string.share)
            }
        }
    }

    fun bind(item: MutableTimerItem) {
        name.text = item.timerName

        val (today, min, max) = item.timerDuration
        val hasRange = min != max

        duration.text = today.produceCompactTime()

        iconDurationRange.isVisible = hasRange
        durationRange.isVisible = hasRange
        if (hasRange) {
            durationRange.text = "${min.produceCompactTime()} - ${max.produceCompactTime()}"
        }

        start.isVisible = item.timerInfo.folderId != FolderEntity.FOLDER_TRASH
    }

    companion object {
        const val MENU_ID_EDIT = 0
        const val MENU_ID_DUPLICATE = 1
        const val MENU_ID_MOVE = 2
        const val MENU_ID_DELETE = 3
        const val MENU_ID_SHARE = 4
    }
}
