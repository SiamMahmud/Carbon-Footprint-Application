package com.example.carbonfootprintcalculation.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.carbonfootprintcalculation.R
import com.example.carbonfootprintcalculation.dashboard.model.FoodResult

class FoodAdapter(private var foodList: MutableList<FoodResult>) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodTypeTv: TextView = itemView.findViewById(R.id.foodTypeTv)
        val quantityTv: TextView = itemView.findViewById(R.id.quantityTv)
        val emissionTv: TextView = itemView.findViewById(R.id.foodEmissionTv)
        val timestampTv: TextView = itemView.findViewById(R.id.foodTimeDateTv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.food_items, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val foodResult = foodList[position]
        holder.foodTypeTv.text = "Food Type : ${foodResult.foodType}"
        holder.quantityTv.text = "Quantity : ${foodResult.quantity}"
        holder.emissionTv.text = "Emission : ${foodResult.emission} kg CO₂"
        holder.timestampTv.text = "Date: ${foodResult.timestamp}"
    }

    override fun getItemCount(): Int = foodList.size
    fun updateData(newFoodList: List<FoodResult>) {
        foodList.clear()
        foodList.addAll(newFoodList)
        notifyDataSetChanged()
    }
}
