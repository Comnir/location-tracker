package com.jefferson.tracker.persistance

import android.content.Context
import android.location.Location
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import io.reactivex.schedulers.Schedulers

class Persistence(private val database: AppDatabase) {
    private val TAG = "Persistence"
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
            instance = Persistence(AppDatabase.getInstance(context))
            return instance
        }
    }

    // TODO: use Kotlin co-routines for DB access
    // https://developer.android.com/kotlin/coroutines

//    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun addLocation(sessionId: Long, location: Location) {
//        coroutineScope.launch {
//            val dbLocation = Location(
//                uid = 0, longitude = location.longitude,
//                latitude = location.latitude, sessionId = sessionId, timestamp = location.time
//            )
//
//            database.locationDao()
//                .insertLocation(dbLocation)
//                .subscribe(
//                    { Log.d(TAG, "Added location to DB.") },
//                    { e ->
//                        Log.e(
//                            TAG,
//                            "Error encountered while inserting location to DB.",
//                            e
//                        )
//                    }) // TODO: decide what to do with the returned Disposable
//        }
        val message = Message()
        message.data.putLong("sessionId", sessionId)
        message.data.putParcelable("location", location)
        message.data.putString("op", "insert")
        handler.sendMessage(message)
    }

    fun locationForSession(sessionId: Long) {
        // TODO: execute on a different thread, because loading from DB is not allowed to execute on MainThread
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
    }

    private class DatabaseOperationHandler(
        val database: AppDatabase,
        looper: Looper
    ) : Handler(looper) {
        private val TAG: String = "DB-op"

        override fun handleMessage(message: Message) {
            super.handleMessage(message)

            val op = message.data.getString("op")
            if ("insert".equals(op)) {
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