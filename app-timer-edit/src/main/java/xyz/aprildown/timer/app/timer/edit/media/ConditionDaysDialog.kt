package xyz.aprildown.timer.app.timer.edit.media

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nex3z.togglebuttongroup.button.CircularToggle
import xyz.aprildown.timer.app.base.data.PreferenceData.startWeekOn
import xyz.aprildown.timer.app.base.utils.WeekdaysFormatter
import xyz.aprildown.timer.app.timer.edit.databinding.DialogConditionDaysBinding
import java.text.DateFormatSymbols
import xyz.aprildown.timer.app.base.R as RBase

internal class ConditionDaysDialog(private val context: Context) {
    fun show(oldConditionDays: List<Boolean>?, func: (List<Boolean>?) -> Unit) {
        val binding = DialogConditionDaysBinding.inflate(LayoutInflater.from(context))
        val dayButtons = binding.layoutConditionDays.children.map { it as CircularToggle }.toList()
        val weekDayOrder = WeekdaysFormatter.WeekDayOrder.fromStartDay(context.startWeekOn)
        val weekdaysStrings = DateFormatSymbols().shortWeekdays

        dayButtons.forEachIndexed { index, toggle ->
            val calendarDay = weekDayOrder.days[index]
            val dayIndex = WeekdaysFormatter.calendarDayToDayIndex(calendarDay)
            toggle.text = weekdaysStrings[calendarDay]
            toggle.isChecked = oldConditionDays?.get(dayIndex) == true
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(RBase.string.edit_condition_days_dialog_title)
            .setView(binding.root)
            .setPositiveButton(RBase.string.ok, null)
            .setNeutralButton(RBase.string.edit_condition_days_clear, null)
            .setNegativeButton(RBase.string.cancel, null)
            .create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val newConditionDays = MutableList(7) { false }
            dayButtons.forEachIndexed { index, toggle ->
                val calendarDay = weekDayOrder.days[index]
                val dayIndex = WeekdaysFormatter.calendarDayToDayIndex(calendarDay)
                newConditionDays[dayIndex] = toggle.isChecked
            }
            dialog.dismiss()
            func.invoke(newConditionDays)
        }
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            dialog.dismiss()
            func.invoke(null)
        }
    }
}
