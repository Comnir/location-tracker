package com.jefferson.tracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import kotlin.random.Random

class SessionActivity : AppCompatActivity() {
    private val TAG = "TRACKING_SESSION"
    private val LOCATION_SETTINGS_FAILURE_REQUST_ID = 102

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)

        val locationRequest = LocationRequest()
            .setInterval(1000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val settingsBuilder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val locationSettingsCheckTask = client.checkLocationSettings(settingsBuilder.build())
        locationSettingsCheckTask.addOnSuccessListener {
            Log.i(TAG,"Location request check succeeded: $it")
            // initialize location request
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (LOCATION_SETTINGS_FAILURE_REQUST_ID == requestCode) {
            Log.i(TAG, "Request result code: $resultCode, with data: $data")
        }
    }
}
