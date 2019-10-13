package com.jefferson.tracker.persistance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Location::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private lateinit var instance: AppDatabase

        fun getInstance(context: Context?): AppDatabase {
            if (null == context) {
                throw IllegalAccessError("Database is not yet initialized. 'Context' should be provided to allow initialization.")
            }

            instance = Room.databaseBuilder(context, AppDatabase::class.java, "trackingdb").build()
            return this.instance
        }
    }

    abstract fun locationDao(): LocationDao
}