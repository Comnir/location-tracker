package com.jefferson.tracker.service

import android.app.IntentService
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.*
import com.jefferson.tracker.persistance.Persistence
import com.jefferson.tracker.session.Session
import java.util.concurrent.atomic.AtomicReference

class LocationTrackingService : IntentService("LocationTrackingService") {
    private val NOTIFICATION_ID: Int = 78
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var persistence: Persistence
    private var sessionId: AtomicReference<Long> = AtomicReference()
    private lateinit var session: Session
    private var lastCallback: LocationCallback? = null

    val locationRequest: LocationRequest by lazy {
        val locationRequest = LocationRequest()
            .setInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest
    }

    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, "onHandleIntent called with $intent")

    }

    val TAG = "LocationTrackingService"
    override fun onBind(intent: Intent?): IBinder? {
        Log.i(TAG, "onBind called")
        return TrackingServiceBinder(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onSed")
        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        persistence = Persistence.getInstance(applicationContext)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun startSession(notification: Notification, newSessionId: Long) {
        Log.i(TAG, "Start tracking session ID $newSessionId")

        val currentCallback: LocationCallback
        synchronized(sessionId) {
            if (null != sessionId.get()) {
                throw IllegalStateException("Session is already in progress - this indicates a bug in the application.")
            }

            sessionId.set(newSessionId)
            currentCallback = locationCallbackForSession(newSessionId)
            lastCallback = currentCallback

            startForeground(NOTIFICATION_ID, notification)

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                currentCallback,
                Looper.getMainLooper()
            )
        }

        session = Session(
            sessionId = newSessionId,
            startTime = System.currentTimeMillis()
        )
    }

    fun stopSession() {
        synchronized(sessionId) {
            if (null == sessionId.get()) {
                Log.e(TAG, "Stopping session, but session ID is missing.")
                return
            }

            fusedLocationClient.removeLocationUpdates(lastCallback)
            stopForeground(true)
            sessionId.set(null)
            lastCallback = null
        }

        persistence.insertSession(session.copy(endTime = System.currentTimeMillis()))
    }

    private fun locationCallbackForSession(sessionIdForCallback: Long): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(location: LocationResult?) {
                super.onLocationResult(location)
                Log.i(TAG, "Location callback called with ${location?.lastLocation}")
                location?.lastLocation?.let {
                    persistence.addLocation(sessionIdForCallback, it)
                }
            }
        }
    }

    class TrackingServiceBinder(val serviceInstance: LocationTrackingService) : Binder() {
        fun service(): LocationTrackingService {
            return serviceInstance
        }
    }

}