package xyz.aprildown.timer.app.timer.list.steplog

import android.content.Context
import android.text.format.DateUtils
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.timer.app.timer.list.R
import xyz.aprildown.timer.domain.entities.StepStampEntity
import xyz.aprildown.timer.app.base.R as RBase

internal data class VisibleStepStamp(
    val id: Int,
    val dateTime: String,
    val timerAndStepName: String,
    val method: String
) {

    fun bind(holder: ViewHolder) {
        holder.run {
            dateTimeText.text = dateTime
            nameText.text = timerAndStepName
            methodText.text = method
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateTimeText: TextView = view.findViewById(R.id.textStepStampDateTime)
        val nameText: TextView = view.findViewById(R.id.textStepStampName)
        val methodText: TextView = view.findViewById(R.id.textStepStampMethod)
    }

    companion object {
        fun fromStepStampEntity(
            stamp: StepStampEntity,
            context: Context
        ): VisibleStepStamp = VisibleStepStamp(
            id = stamp.id,
            dateTime = DateUtils.formatDateTime(
                context,
                stamp.timestamp,
                DateUtils.FORMAT_SHOW_DATE or
                    DateUtils.FORMAT_SHOW_TIME or
                    DateUtils.FORMAT_ABBREV_MONTH
            ),
            timerAndStepName = "%s · %s".format(stamp.timerName, stamp.stepName),
            method = when (stamp.confirmMethod) {
                StepStampEntity.ConfirmMethod.AUTO -> context.getString(RBase.string.step_log_method_auto)
                StepStampEntity.ConfirmMethod.MANUAL -> context.getString(RBase.string.step_log_method_manual)
            }
        )
    }
}
