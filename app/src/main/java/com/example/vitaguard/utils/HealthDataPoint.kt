package com.example.vitaguard.utils

data class HealthDataPoint(
    val GOOD_HEALTH: String = "g",
    val MID_HEALTH: String = "m",
    val BAD_HEALTH: String = "b",
    var bpm: Int,
    var sp02: Int,
    var bp: Int,
    var code: String
)
