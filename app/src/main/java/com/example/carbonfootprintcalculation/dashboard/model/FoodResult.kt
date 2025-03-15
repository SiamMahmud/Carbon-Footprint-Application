package com.example.carbonfootprintcalculation.dashboard.model

data class FoodResult(
    val foodType : String,
    val quantity : Double = 0.0,
    val emission: Double = 0.0,
    val timestamp: String = ""
)
