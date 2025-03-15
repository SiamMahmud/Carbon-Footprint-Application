package com.example.carbonfootprintcalculation.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.dashboard.model.PublicTransportResult

class PublicTransportAdapter(private var publicTransportList: MutableList<PublicTransportResult>) :
    RecyclerView.Adapter<PublicTransportAdapter.publicTransportViewHolder>() {

    inner class publicTransportViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val publicTransportTypeTv: TextView = itemView.findViewById(R.id.publicTransportTypeTv)
        val travelDistanceTv: TextView = itemView.findViewById(R.id.publicTransportDisTv)
        val publicTransportEmissionRateTv: TextView = itemView.findViewById(R.id.emissionRTv)
        val timeDateTv: TextView = itemView.findViewById(R.id.timeDateTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): publicTransportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.public_transport_items, parent, false)
        return publicTransportViewHolder(view)
    }

    override fun onBindViewHolder(holder: publicTransportViewHolder, position: Int) {
        val publicTransportResult = publicTransportList[position]
        holder.publicTransportTypeTv.text = "Public Transport Type : ${publicTransportResult.transportType}"
        holder.travelDistanceTv.text = "Distance: ${publicTransportResult.distance} km"
        holder.publicTransportEmissionRateTv.text = "Emission Rate : ${publicTransportResult.emissionRate} kg CO₂"
        holder.timeDateTv.text = "Date: ${publicTransportResult.timestamp}"
    }

    override fun getItemCount(): Int = publicTransportList.size
    fun updateData(newTransportList: List<PublicTransportResult>) {
        publicTransportList.clear()
        publicTransportList.addAll(newTransportList)
        notifyDataSetChanged()
    }
}
