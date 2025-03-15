package com.example.carbonfootprintcalculation.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.dashboard.model.CarResult

class CarAdapter(private var carList: MutableList<CarResult>) :
    RecyclerView.Adapter<CarAdapter.CarViewHolder>() {

    inner class CarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val carTypeTv: TextView = itemView.findViewById(R.id.carTypeTv)
        val fuelTypeTv: TextView = itemView.findViewById(R.id.fuelTypeTv)
        val distanceTv: TextView = itemView.findViewById(R.id.distanceTv)
        val emissionRateTv: TextView = itemView.findViewById(R.id.emissionRateTv)
        val timestampTv: TextView = itemView.findViewById(R.id.timestampTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.car_result_items, parent, false)
        return CarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarViewHolder, position: Int) {
        val carResult = carList[position]
        holder.carTypeTv.text = "Car Type: ${carResult.carType}"
        holder.fuelTypeTv.text = "Fuel Type: ${carResult.fuelType}"
        holder.distanceTv.text = "Distance: ${carResult.distance} km"
        holder.emissionRateTv.text = "Emission: ${carResult.emissionRate} kg CO₂"
        holder.timestampTv.text = "Date: ${carResult.timestamp}"
    }

    override fun getItemCount(): Int = carList.size
    fun updateData(newCarList: List<CarResult>) {
        carList.clear()
        carList.addAll(newCarList)
        notifyDataSetChanged()
    }
}
