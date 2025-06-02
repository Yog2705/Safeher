package com.example.safeher

data class SafePlaceType(val type: String, val displayName: String)

val allSafePlaceTypes = listOf(
    SafePlaceType("police", "Police Station"),
    SafePlaceType("hospital", "Hospital"),
    SafePlaceType("fire_station", "Fire Station"),
    SafePlaceType("pharmacy", "Pharmacy"),
    SafePlaceType("community_center", "Community Center"),
    SafePlaceType("local_government_office", "Government Office"),
    SafePlaceType("train_station", "Train Station"),
    SafePlaceType("bus_station", "Bus Station"),
    SafePlaceType("shopping_mall", "Shopping Mall"),
)
