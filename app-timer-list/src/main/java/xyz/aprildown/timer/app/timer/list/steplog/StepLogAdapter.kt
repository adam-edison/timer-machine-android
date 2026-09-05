package xyz.aprildown.timer.app.timer.list.steplog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xyz.aprildown.timer.app.timer.list.R

internal class StepLogAdapter : RecyclerView.Adapter<VisibleStepStamp.ViewHolder>() {

    private val stamps = mutableListOf<VisibleStepStamp>()

    override fun getItemCount(): Int = stamps.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisibleStepStamp.ViewHolder {
        return VisibleStepStamp.ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_step_stamp, parent, false)
        )
    }

    override fun onBindViewHolder(holder: VisibleStepStamp.ViewHolder, position: Int) {
        stamps[position].bind(holder)
    }

    fun set(new: List<VisibleStepStamp>) {
        stamps.clear()
        stamps.addAll(new)
        notifyDataSetChanged()
    }
}
