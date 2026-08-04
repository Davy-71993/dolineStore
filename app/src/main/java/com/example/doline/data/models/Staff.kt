package com.example.doline.data.models

data class Staff(
    val fullNames: String,
    val username: String,
    val passKey: String,
    val role: String
)

data class ActiveSession (
    val staff: Staff,
    val device: String,
    val duration: Float,
    val location: String
)

