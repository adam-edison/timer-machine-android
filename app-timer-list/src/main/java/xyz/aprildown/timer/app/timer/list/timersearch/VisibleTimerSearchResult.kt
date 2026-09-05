package xyz.aprildown.timer.app.timer.list.timersearch

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.timer.app.timer.list.R

internal data class VisibleTimerSearchResult(
    val timerId: Int,
    val timerName: String,
    val folderName: String,
) {

    fun bind(holder: ViewHolder) {
        holder.run {
            nameText.text = timerName
            folderText.text = folderName
        }
    }

    class ViewHolder(view: View, onClick: (position: Int) -> Unit) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textTimerSearchResultName)
        val folderText: TextView = view.findViewById(R.id.textTimerSearchResultFolder)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick.invoke(position)
                }
            }
        }
    }
}
