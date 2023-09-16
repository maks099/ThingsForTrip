package com.example.thingsfortrip.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class ListData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val time: Long
)