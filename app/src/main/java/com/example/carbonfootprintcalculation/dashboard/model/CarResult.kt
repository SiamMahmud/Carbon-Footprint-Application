package com.example.carbonfootprintcalculation.dashboard.model

data class CarResult(
    val carType: String = "",
    val fuelType: String = "",
    val distance: Double = 0.0,
    val emissionRate: Double = 0.0,
    val timestamp: String = ""
)
