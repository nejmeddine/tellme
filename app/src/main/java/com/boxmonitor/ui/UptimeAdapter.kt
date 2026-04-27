package com.boxmonitor.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.boxmonitor.R
import com.boxmonitor.data.UptimeRecord

class UptimeAdapter(private val items: List<UptimeRecord>) :
    RecyclerView.Adapter<UptimeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvStartEnd: TextView = view.findViewById(R.id.tvStartEnd)
        val tvDuration: TextView = view.findViewById(R.id.tvDuration)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_uptime, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDate.text = item.date
        val end = item.endTime ?: "en cours"
        holder.tvStartEnd.text = "${item.startTime} → $end"
        val hours = item.durationMinutes / 60
        val mins = item.durationMinutes % 60
        holder.tvDuration.text = if (hours > 0) "${hours}h ${mins}min" else "${mins}min"
    }

    override fun getItemCount() = items.size
}
