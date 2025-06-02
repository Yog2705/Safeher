package com.example.safeher

import com.google.android.gms.maps.model.LatLng

data class Place(
    val name: String,
    val address: String,
    val location: LatLng,
    val distance: Float
)
