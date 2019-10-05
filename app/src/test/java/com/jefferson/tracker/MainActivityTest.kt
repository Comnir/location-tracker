package com.jefferson.tracker

import android.app.Application
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import kotlinx.android.synthetic.main.activity_main.*
import org.junit.Assert.assertNotEquals
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLocationManager
import org.robolectric.util.Logger


@Config(sdk = [Build.VERSION_CODES.O_MR1], shadows = [ShadowLocationManager::class], application = Application::class)
@RunWith(RobolectricTestRunner::class)
class MainActivityTest {
    fun setup() {
        System.setProperty("robolectric.logging.enabled", "true")

        val context = ApplicationProvider.getApplicationContext<Context>()
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val shadowLocationManager = shadowOf(locationManager)

        val location = Location("GPS") as Location
        location.accuracy = 42.0f
        location.altitude = 20.1
        location.latitude = 15.2
        location.longitude = 12.4

        shadowLocationManager.setLastKnownLocation("GPS", location)

    }

    @Ignore("Fails, because location update is not delivered by Robolectric...")
    @Test
    fun testLocationUpdate() {
        // This test doesn't assert

        val activity = Robolectric.setupActivity(MainActivity::class.java) as MainActivity
        val locationTextView = activity.locationView
        val oldText = locationTextView.text
        val button = activity.findViewById(R.id.updateLocationButton) as Button
        button.performClick()

        val newText = locationTextView.text

        assertNotEquals(newText, oldText)
    }

}