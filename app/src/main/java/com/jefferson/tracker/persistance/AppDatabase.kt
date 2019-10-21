package com.jefferson.tracker.persistance

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jefferson.tracker.session.Session
import com.jefferson.tracker.session.SessionDao
import com.jefferson.tracker.session.location.Location
import com.jefferson.tracker.session.location.LocationDao

@Database(entities = [Location::class, Session::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private lateinit var instance: AppDatabase

        fun getInstance(context: Context?): AppDatabase {
            if (null == context) {
                throw IllegalAccessError("Database is not yet initialized. 'Context' should be provided to allow initialization.")
            }

            synchronized(AppDatabase::class.java) {
                if (!::instance.isInitialized) {
                    instance =
                        Room.databaseBuilder(context, AppDatabase::class.java, "trackingdb").build()
                }
            }
            return instance
        }
    }

    abstract fun locationDao(): LocationDao
    abstract fun sessionDao(): SessionDao
}