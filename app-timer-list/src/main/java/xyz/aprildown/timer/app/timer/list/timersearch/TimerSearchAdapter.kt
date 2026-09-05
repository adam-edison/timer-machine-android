package xyz.aprildown.timer.app.timer.list.timersearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.timer.app.timer.list.R

internal class TimerSearchAdapter(
    private val onClickResult: (VisibleTimerSearchResult) -> Unit,
) : RecyclerView.Adapter<VisibleTimerSearchResult.ViewHolder>() {

    private val results = mutableListOf<VisibleTimerSearchResult>()

    override fun getItemCount(): Int = results.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisibleTimerSearchResult.ViewHolder {
        return VisibleTimerSearchResult.ViewHolder(
            view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timer_search_result, parent, false),
            onClick = { position -> onClickResult.invoke(results[position]) },
        )
    }

    override fun onBindViewHolder(holder: VisibleTimerSearchResult.ViewHolder, position: Int) {
        results[position].bind(holder)
    }

    fun set(new: List<VisibleTimerSearchResult>) {
        results.clear()
        results.addAll(new)
        notifyDataSetChanged()
    }
}
