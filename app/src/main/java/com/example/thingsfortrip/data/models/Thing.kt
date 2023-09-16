package com.example.thingsfortrip.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Thing(
    @PrimaryKey(autoGenerate = true) val index: Int = 0,
    val name: String = "",
    var isChecked: Boolean = false,
    var listCode: Int = -1
)