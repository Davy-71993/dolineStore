package com.example.doline.data.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime

data class Note(
    val title: String,
    val body: String,
    val date: LocalDateTime
)
