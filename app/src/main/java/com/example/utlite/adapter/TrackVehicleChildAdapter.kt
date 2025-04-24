package com.example.utlite.adapter

import com.example.utlite.model.vehicletracking.MilestoneActionsTracking



import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.utlite.R

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class TrackVehicleChildAdapter(
    private val mList: List<MilestoneActionsTracking>,
    private val context: Context
) : RecyclerView.Adapter<TrackVehicleChildAdapter.NestedViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NestedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_child, parent, false)
        return NestedViewHolder(view)
    }

    override fun onBindViewHolder(holder: NestedViewHolder, position: Int) {
        val item = mList[position]

        holder.tvLabel.text = item.milestoneAction

        val iconRes = when (item.status) {
            "Completed" -> R.drawable.ic_tick_small_green
            "Open", "ReOpen" -> R.drawable.ic_open_small_orange
            "Cancelled" -> R.drawable.ic_close_small_red
            "Pending" -> R.drawable.ic_pending_small_blue
            "Failed" -> R.drawable.ic_failed_small_blue
            else -> null
        }

        iconRes?.let {
            holder.ivStatus.setImageDrawable(ResourcesCompat.getDrawable(context.resources, it, context.theme))
        }

        holder.tvStatus.text = item.completionTime?.let {
            try {
                formattedDate(it)
            } catch (e: ParseException) {
                e.printStackTrace()
                "NA"
            }
        } ?: "NA"

        Log.e("Size", mList.size.toString())
    }

    private fun formattedDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(inputDate)
        val formattedDate = outputFormat.format(date ?: Date())
        Log.e("datetime", formattedDate)
        return formattedDate
    }

    override fun getItemCount(): Int = mList.size

    inner class NestedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvLabel: TextView = itemView.findViewById(R.id.label)
        val tvStatus: TextView = itemView.findViewById(R.id.status)
        val ivStatus: ImageView = itemView.findViewById(R.id.iv_status)
    }
}
