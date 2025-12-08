package com.internetofthings.displaycontroller

import android.content.Context
import androidx.room.Room
import com.internetofthings.displaycontroller.helpers.Database
import com.internetofthings.displaycontroller.models.GPSModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GPSDataManager(context: Context) {
    private val db: Database = Room.databaseBuilder(
        context.applicationContext,
        Database::class.java,
        "my_gps_db"
    ).build()

    private val gpsDAO = db.gpsDAO()

    suspend fun saveGPSData(gpsModel: GPSModel) {
        withContext(Dispatchers.IO) {
            gpsDAO.insert(gpsModel)
        }
    }

    suspend fun getAllGPSData(): List<GPSModel> {
        return withContext(Dispatchers.IO) {
            gpsDAO.getAllGPS()
        }
    }

    suspend fun deleteGPSData(gpsModel: GPSModel) {
        withContext(Dispatchers.IO) {
            gpsDAO.delete(gpsModel)
        }
    }

    suspend fun clearAllGPSData() {
        withContext(Dispatchers.IO) {
            val allData = gpsDAO.getAllGPS()
            allData.forEach { gpsDAO.delete(it) }
        }
    }
}

