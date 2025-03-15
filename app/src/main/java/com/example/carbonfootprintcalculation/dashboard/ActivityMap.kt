package com.example.carbonfootprintcalculation.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.carbonfootprintcalculation.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ActivityMap : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var origin: LatLng? = null
    private var destination: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        mMap.setOnMapClickListener { latLng ->
            if (origin == null) {
                origin = latLng
                mMap.addMarker(MarkerOptions().position(latLng).title("Start Location"))
            } else if (destination == null) {
                destination = latLng
                mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))
                calculateDistance()
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    origin = LatLng(location.latitude, location.longitude)
                    mMap.addMarker(MarkerOptions().position(origin!!).title("Current Location"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin!!, 15f))
                }
            }
        }
    }

    private fun calculateDistance() {
        if (origin != null && destination != null) {
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin!!.latitude},${origin!!.longitude}&destination=${destination!!.latitude},${destination!!.longitude}&key=YOUR_API_KEY"

            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()

            Thread {
                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                val json = JSONObject(responseData!!)
                val distance = json.getJSONArray("routes")
                    .getJSONObject(0)
                    .getJSONArray("legs")
                    .getJSONObject(0)
                    .getJSONObject("distance")
                    .getString("text")

                runOnUiThread {
                    val resultIntent = Intent()
                    resultIntent.putExtra("DISTANCE", distance)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }.start()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }
}
