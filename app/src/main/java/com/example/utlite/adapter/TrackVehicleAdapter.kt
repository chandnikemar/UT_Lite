package com.example.utlite.adapter

import com.example.utlite.model.vehicletracking.JobMilestone
import com.example.utlite.model.vehicletracking.MilestoneActionsTracking


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.utlite.R

class TrackVehicleAdapter(
    private val context: Context,
    private val milestones: List<JobMilestone>
) : RecyclerView.Adapter<TrackVehicleAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var list: List<MilestoneActionsTracking> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.track_vehicle, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = milestones[position]
        holder.tvMilestone.text = "${model.milestone}-${model.milestioneEvent}"
        holder.tvLocationName.text = model.locationName

        when (model.status) {
            "Pending" -> holder.icStatus.visibility = View.GONE
            "Completed" -> {
                holder.icStatus.visibility = View.VISIBLE
                holder.icStatus.setImageResource(R.drawable.ic_tick_new_green)
            }
            "Open" -> {
                holder.icStatus.visibility = View.VISIBLE
                holder.icStatus.setImageResource(R.drawable.ic_tick_new_orange)
            }
        }

        val isExpandable = model.isExpandable
        holder.expandableLayout.visibility = if (isExpandable) View.VISIBLE else View.GONE
        holder.mArrowImage.setImageResource(if (isExpandable) R.drawable.arrow_up else R.drawable.arrow_down)

        val adapter = model.milestoneActionsTracking?.let { TrackVehicleChildAdapter(it, context) }
        holder.nestedRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            this.adapter = adapter
        }

        holder.linearLayout.setOnClickListener {
            model.isExpandable = !model.isExpandable
            list = model.milestoneActionsTracking!!
            notifyItemChanged(holder.adapterPosition)
        }
    }

    override fun getItemCount(): Int = milestones.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMilestone: TextView = itemView.findViewById(R.id.tvMilestone)
        val tvLocationName: TextView = itemView.findViewById(R.id.tvLocationName)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linear_layout)
        val expandableLayout: RelativeLayout = itemView.findViewById(R.id.expandable_layout)
        val mArrowImage: ImageView = itemView.findViewById(R.id.arro_imageview)
        val icStatus: ImageView = itemView.findViewById(R.id.icStatus)
        val nestedRecyclerView: RecyclerView = itemView.findViewById(R.id.child_rv)
    }
}
