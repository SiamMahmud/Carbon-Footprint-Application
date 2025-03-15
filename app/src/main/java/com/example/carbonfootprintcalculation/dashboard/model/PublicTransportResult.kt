package com.example.carbonfootprintcalculation.dashboard.model

data class PublicTransportResult(
    val transportType : String,
    val distance: Double = 0.0,
    val emissionRate: Double = 0.0,
    val timestamp: String = ""
)
