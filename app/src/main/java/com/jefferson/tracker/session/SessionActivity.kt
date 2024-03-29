package com.jefferson.tracker.session

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.jefferson.tracker.R
import com.jefferson.tracker.service.LocationTrackingService


class SessionActivity : AppCompatActivity() {
    private val REQUEST_PERMISSIONS_REQUEST_CODE: Int = 6
    private var trackingService: LocationTrackingService? = null
    private lateinit var locationRequest: LocationRequest
    private var serviceIsBound = false
    private val TAG = "TRACKING_SESSION"
    private val LOCATION_SETTINGS_FAILURE_REQUST_ID = 102

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            serviceIsBound = true
            if (binder is LocationTrackingService.TrackingServiceBinder) {
                trackingService = binder.serviceInstance
                locationRequest = binder.serviceInstance.locationRequest
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
            serviceIsBound = false
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        serviceIntent = Intent(this, LocationTrackingService::class.java)

        setContentView(R.layout.activity_session)
    }

    private fun checkLocationSettings() {
        Log.i(
            TAG,
            "Location settings will checked, since Location/GPS should be enabled for location tracking."
        )

        if (!serviceIsBound) {
            Log.w(TAG, "Location settings won't be checked - tracking service is not available")
            return
        }

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val locationSettingsCheckTask = client.checkLocationSettings(settingsBuilder.build())
        locationSettingsCheckTask.addOnSuccessListener {
            Log.i(TAG, "Location request check succeeded: $it")
        }

        locationSettingsCheckTask.addOnFailureListener {
            Log.w(TAG, "Location request is not supported: $it")
            if (it is ResolvableApiException) {
                Log.i(TAG, "Request user intervention.")
                it.startResolutionForResult(this, LOCATION_SETTINGS_FAILURE_REQUST_ID)
                Log.i(TAG, "Resolution returned.")
            }
        }
    }

    private lateinit var serviceIntent: Intent

    override fun onStart() {
        super.onStart()

        if (!checkPermission()) {
            Log.i(TAG, "Location permission missing")
            requestPermission()
        } else {
            Log.i(TAG, "Location permission already granted - won't ask for it.")
        }

        if (bindService(
                serviceIntent,
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        ) {
            Log.i(TAG, "Service bind returned 'true'")
        } else {
            Log.w(TAG, "Service didn't bind")
        }
    }

    override fun onStop() {
        super.onStop()

        if (serviceIsBound) {
            unbindService(serviceConnection)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (LOCATION_SETTINGS_FAILURE_REQUST_ID == requestCode) {
            Log.i(TAG, "Request result code: $resultCode, with data: $data")
        }
    }

    val notification: Notification by lazy {
        createNotification()
    }

    private fun createNotification(): Notification {
        val builder = NotificationCompat.Builder(this)
            .setContentText(getText(R.string.notification_message))
            .setContentTitle(getText(R.string.notification_title))
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_tracking_status)
            .setTicker(getText(R.string.ticker_text))
            .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId("location-tracking-channel") // Channel ID
        }

        return builder.build()
    }

    fun startSession(view: View) {
        if (trackingService?.sessionIsActive() == true) {
            val message = "A previous session is already active..."
            Log.w(TAG, message)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            return
        }

        if (view is Button) {
            view.isEnabled = false
        }

        try {
            checkLocationSettings()

            Log.i(TAG, "Service started? " + startService(serviceIntent))

            val sessionId = System.currentTimeMillis()
            trackingService?.startSession(notification, sessionId)
            findViewById<Button>(R.id.stopButton).isEnabled = true
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to start session: $e", Toast.LENGTH_LONG).show()
            view.isEnabled = true
        }
    }

    fun stopSession(view: View) {
        trackingService?.stopSession()
        stopService(serviceIntent)

        findViewById<Button>(R.id.startButton).isEnabled = true
        if (view is Button) {
            view.isEnabled = false
        }
    }

    private fun checkPermission(): Boolean {
        return PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        )
    }

    private fun requestPermission() {
        val shouldShowRequestPermissionRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)

        if (shouldShowRequestPermissionRationale) {
            Log.i(TAG, "Displaying permission rationale")
            Snackbar.make(
                findViewById(R.id.session_activity_id),
                "Location permission is required for tracking!!!",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction("Ok") {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            ACCESS_FINE_LOCATION
                        ), REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                }
                .show()
        } else {
            Log.i(TAG, "Requesting permission without further ado")
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    ACCESS_FINE_LOCATION
                ), REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }
}
