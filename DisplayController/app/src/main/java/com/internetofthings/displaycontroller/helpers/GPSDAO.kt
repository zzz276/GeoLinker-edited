package com.internetofthings.displaycontroller.helpers

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.internetofthings.displaycontroller.models.GPSModel

@Dao
interface GPSDAO {
    @Insert
    suspend fun insert(gps: GPSModel)

    @Query("SELECT * FROM gps_table ORDER BY id DESC")
    suspend fun getAllGPS(): List<GPSModel>
    
    @Delete
    suspend fun delete(gps: GPSModel)
}

