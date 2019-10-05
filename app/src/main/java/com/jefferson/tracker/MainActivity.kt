package com.jefferson.tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    val TAG = "TRACKER_MAIN"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val updateLocationButton = findViewById<Button>(R.id.updateLocationButton) as Button
        updateLocationButton.setOnClickListener {
            val locationTask = fusedLocationClient.lastLocation
            locationTask.addOnSuccessListener {
                Log.i(TAG, "Successful location retrieval: $it")
                it?.let { findViewById<TextView>(R.id.locationView).setText(it.toString()) }
            }
            locationTask.addOnFailureListener {
                Log.i(TAG, "Failed to update location: $it")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val available =
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        Log.i(TAG, "Google play services availability: " + ConnectionResult(available))
    }

    fun newSession(view: View) {
        startActivity(Intent(this, SessionActivity::class.java))
    }
}
