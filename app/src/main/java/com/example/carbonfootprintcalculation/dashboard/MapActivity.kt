package com.example.carbonfootprintcalculation.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.carbonfootprintcalculation.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private var origin: LatLng? = null
    private var destination: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var distanceTextView: TextView
    private lateinit var okButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        distanceTextView = findViewById(R.id.distanceTextView)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        okButton = findViewById(R.id.okButton)
        okButton.setOnClickListener {
            val fullDistanceText = distanceTextView.text.toString()
            val distanceValue = fullDistanceText.split(" ")[1]
            val resultIntent = Intent()
            resultIntent.putExtra("DISTANCE", distanceValue)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        }


        checkLocationEnabled()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
            requestLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        }

        mMap.setOnMapClickListener { latLng ->
            if (origin == null) {
                Toast.makeText(this, "Waiting for current location...", Toast.LENGTH_SHORT).show()
                return@setOnMapClickListener
            }

            destination = latLng
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(origin!!).title("Current Location"))
            mMap.addMarker(MarkerOptions().position(latLng).title("Destination"))
            okButton.visibility = View.VISIBLE
            calculateDistance()
        }
    }
    private fun calculateDistance() {
        if (origin == null || destination == null) return

        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${origin!!.latitude},${origin!!.longitude}&destination=${destination!!.latitude},${destination!!.longitude}&key=AIzaSyA7yXttdhwVQB6Dj0rPxlavyqFV3C47hmo"

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = OkHttpClient().newCall(Request.Builder().url(url).build()).execute()
                val responseData = response.body?.string()

                if (!responseData.isNullOrEmpty()) {
                    val json = JSONObject(responseData)
                    val routes = json.optJSONArray("routes")

                    if (routes != null && routes.length() > 0) {
                        val legs = routes.getJSONObject(0).optJSONArray("legs")

                        if (legs != null && legs.length() > 0) {
                            val distanceObj = legs.getJSONObject(0).optJSONObject("distance")

                            if (distanceObj != null) {
                                val distanceText = distanceObj.getString("text") // Example: "12.5 km"
                                val distanceValue = distanceText.split(" ")[0] // Extracts "12.5"

                                val overviewPolyline = routes.getJSONObject(0).optJSONObject("overview_polyline")
                                val polyline = overviewPolyline?.optString("points", "")

                                withContext(Dispatchers.Main) {
                                    distanceTextView.text = "Distance: $distanceValue km" // Display full text
                                    Log.d("MapActivity", "Distance: $distanceValue km")

                                    if (!polyline.isNullOrEmpty()) {
                                        drawRoute(polyline)
                                    } else {
                                        Toast.makeText(this@MapActivity, "No route found", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MapActivity", "Error fetching distance", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MapActivity, "Failed to get distance", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun drawRoute(encodedPolyline: String) {
        val decodedPath = PolyUtil.decode(encodedPolyline)
        mMap.addPolyline(
            PolylineOptions()
                .addAll(decodedPath)
                .width(10f)
                .color(ContextCompat.getColor(this, R.color.colorPrimary))
        )
    }

    private fun checkLocationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder(this)
                .setMessage("GPS is disabled. Please enable location services.")
                .setPositiveButton("Enable") { _, _ ->
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            requestLocationUpdates()
        }
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    origin = LatLng(location.latitude, location.longitude)
                    updateMapLocation(origin!!)
                }
            }
        }
    }

    private fun updateMapLocation(location: LatLng) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        mMap.addMarker(MarkerOptions().position(location).title("Current Location"))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST = 1
    }
}
