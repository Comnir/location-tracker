package com.jefferson.tracker.persistance

import android.content.Context
import android.location.Location
import android.os.*
import android.util.Log
import androidx.lifecycle.LiveData
import io.reactivex.schedulers.Schedulers

class Persistence(
    private val database: AppDatabase,
    locationDao: LocationDao,
    sessionDao: SessionDao
) {
    private val sessions: LiveData<List<Session>> = sessionDao.allSessions()
    private val TAG = "Persistence"
    private val locations = locationDao.allLocations()

    private val databaseHandlerThread: HandlerThread by lazy {
        val temp = HandlerThread("db-operation-handler")
        temp.start()
        temp
    }

    private val handler: Handler by lazy {
        DatabaseOperationHandler(database, databaseHandlerThread.looper)
    }

    companion object {
        private lateinit var instance: Persistence
        fun getInstance(context: Context?): Persistence {
            val database = AppDatabase.getInstance(context)
            instance = Persistence(
                database,
                database.locationDao(),
                database.sessionDao()
            )
            return instance
        }
    }

    // TODO: use Kotlin co-routines for DB access
    // https://developer.android.com/kotlin/coroutines

    fun addLocation(sessionId: Long, location: Location) {
        val message = Message()
        message.data.putLong("sessionId", sessionId)
        message.data.putParcelable("location", location)
        message.data.putString("op", "insert-location")
        handler.sendMessage(message)
    }

    fun locationForSession(sessionId: Long) {
        object : AsyncTask<String, String, Void?>() {
            override fun doInBackground(vararg params: String?): Void? {
                database.locationDao()
                    .locationsForSession(sessionId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        {
                            Log.d(TAG, "Loaded locations")
                            it.forEach { Log.d(TAG, "Location: $it") }
                        },
                        { Log.e(TAG, "Failed to load locations. ", it) }
                    )

                return null
            }
        }
    }

    fun allLocations(): LiveData<List<com.jefferson.tracker.persistance.Location>> {
        return locations
    }

    fun allSessions(): LiveData<List<Session>> {
        return sessions
    }

    fun insertSession(session: Session) {
        Log.i(TAG, "Deffer execution of session insertion")
        AsyncRunner.run {
            Log.i(TAG, "Execute session insertion")
            database.sessionDao()
                .addSession(session)
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { Log.d(TAG, "Session inserted to DB") },
                    { Log.e(TAG, "Error during session insertion") }
                ) // Disposable ignored...

            return@run null
        }
    }

    private class DatabaseOperationHandler(
        val database: AppDatabase,
        looper: Looper
    ) : Handler(looper) {
        private val TAG: String = "DB-op"

        override fun handleMessage(message: Message) {
            super.handleMessage(message)

            val op = message.data.getString("op")
            if ("insert-location" == op) {
                val sessionId = message.data.getLong("sessionId")
                val location = message.data.getParcelable<Location>("location")

                if (null == location) {
                    Log.w(TAG, "Can't insert location, because location information is missing.")
                    return
                }

                val dbLocation = Location(
                    uid = 0, longitude = location.longitude,
                    latitude = location.latitude, sessionId = sessionId, timestamp = location.time
                )

                database.locationDao()
                    .insertLocation(dbLocation)
                    .subscribe(
                        { Log.d(TAG, "Added location to DB.") },
                        { e ->
                            Log.e(
                                TAG,
                                "Error encountered while inserting location to DB.",
                                e
                            )
                        }) // TODO: decide what to do with the returned Disposable
            } else {
                Log.e(TAG, "Unhandled DB operation $op. Message: $message")
            }

        }
    }

}