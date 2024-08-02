package com.example.vitaguard.utils

const val GOOD_HEALTH: String = "no"
const val MID_HEALTH: String = "minor"
const val BAD_HEALTH: String = "major"
data class HealthDataPoint(
    var bpm: Double,
    var sp02: Double,
    var code: String,
    var date: String
)
