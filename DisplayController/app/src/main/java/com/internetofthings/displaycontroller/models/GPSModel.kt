package com.internetofthings.displaycontroller.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gps_table")

data class GPSModel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val time: String,
)

