package com.internetofthings.displaycontroller.helpers

import androidx.room.Database
import androidx.room.RoomDatabase
import com.internetofthings.displaycontroller.models.GPSModel

@Database(entities = [GPSModel::class], version = 1)
abstract class Database: RoomDatabase() {
    abstract fun gpsDAO(): GPSDAO
}

