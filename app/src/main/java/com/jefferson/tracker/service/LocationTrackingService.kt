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

class LocationTrackingService : IntentService("LocationTrackingService") {
    private val NOTIFICATION_ID: Int = 78
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    val locationRequest: LocationRequest by lazy {
        val locationRequest = LocationRequest()
            .setInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(location: LocationResult?) {
            super.onLocationResult(location)
            Log.i(TAG, "Location callback called with $location")
        }
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
        Log.i(TAG, "onStartCommand called")
        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "One-time call to onCreate")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    fun startSession(notification: Notification) {
        Log.i(TAG, "Start tracking session - will start service.")

        startForeground(NOTIFICATION_ID, notification)

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    fun stopSession() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
    }

    class TrackingServiceBinder(val serviceInstance: LocationTrackingService) : Binder() {
        fun service(): LocationTrackingService {
            return serviceInstance
        }
    }

}