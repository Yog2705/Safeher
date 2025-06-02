package com.example.safeher

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import android.util.Log

class SafePlaceActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val apiKey = "AIzaSyCxvi_62pH9t4EPCzau3PvqDJ2JhJYVCXU" // Replace with your actual API key
    private var currentLocation: Location? = null
    private lateinit var placeTypeSpinner: Spinner
    private lateinit var searchButton: Button
    private lateinit var btnShowAll: Button
    private var selectedType: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_place)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        placeTypeSpinner = findViewById(R.id.placeTypeSpinner)
        searchButton = findViewById(R.id.btnFilterTypes)
        btnShowAll = findViewById(R.id.btnShowAll)

        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.place_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        placeTypeSpinner.adapter = adapter

        placeTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>, view: android.view.View, position: Int, id: Long
            ) {
                selectedType = when (position) {
                    0 -> null // First item is "All" or "Select Type"
                    1 -> "police"
                    2 -> "hospital"
                    3 -> "fire_station"
                    4 -> "atm"
                    5 -> "pharmacy"
                    6 -> "restaurant"
                    else -> null
                }
                Log.d("SafePlace", "Selected type: $selectedType")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedType = null
            }
        }

        searchButton.setOnClickListener {
            currentLocation?.let { location ->
                selectedType?.let { type ->
                    fetchNearbyPlaces(LatLng(location.latitude, location.longitude), type)
                } ?: Toast.makeText(this, "Please select a place type", Toast.LENGTH_SHORT).show()
            } ?: Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }

        btnShowAll.setOnClickListener {
            currentLocation?.let { location ->
                fetchNearbyPlaces(LatLng(location.latitude, location.longitude), null)
            } ?: Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): android.view.View? = null
            override fun getInfoContents(marker: Marker): android.view.View {
                val view = layoutInflater.inflate(R.layout.custom_info_window, null)
                val title = view.findViewById<TextView>(R.id.title)
                val snippet = view.findViewById<TextView>(R.id.snippet)
                title.text = marker.title
                snippet.text = marker.snippet
                return view
            }
        })

        enableMyLocation()
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1
            )
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            currentLocation = location
            location?.let {
                val currentLatLng = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f))
            }
        }
    }

    private fun fetchNearbyPlaces(location: LatLng, placeType: String?) {
        val radius = 10000 // in meters
        val typeParam = placeType?.let { "&type=$it" } ?: ""
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=${location.latitude},${location.longitude}" +
                "&radius=$radius" +
                typeParam +
                "&key=$apiKey"  // use your apiKey variable
        Log.d("SafePlace", "Fetching places from URL: $url") // Log the full URL

        val request = Request.Builder().url(url).build()
        val client = OkHttpClient()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@SafePlaceActivity, "Failed to fetch places", Toast.LENGTH_SHORT).show()
                }
                Log.e("SafePlace", "API call failed: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d("SafePlace", "API response: $responseBody")
                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val results = json.getJSONArray("results")

                    runOnUiThread {
                        mMap.clear()
                        for (i in 0 until results.length()) {
                            val place = results.getJSONObject(i)
                            val locationJson = place.getJSONObject("geometry").getJSONObject("location")
                            val lat = locationJson.getDouble("lat")
                            val lng = locationJson.getDouble("lng")
                            val name = place.getString("name")
                            val vicinity = place.optString("vicinity", "No address available")

                            mMap.addMarker(
                                MarkerOptions()
                                    .position(LatLng(lat, lng))
                                    .title(name)
                                    .snippet(vicinity)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            )
                        }

                        if (results.length() == 0) {
                            Toast.makeText(this@SafePlaceActivity, "No places found.", Toast.LENGTH_SHORT).show()
                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f))
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@SafePlaceActivity, "Error fetching data", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("SafePlace", "API response error: ${response.code}")
                }
            }
        })
    }
}
