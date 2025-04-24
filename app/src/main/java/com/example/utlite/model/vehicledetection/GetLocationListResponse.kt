package com.example.utlite.model.vehicledetection

data class GetLocationListResponse(
    val status: String,
    val statusMessage: String,
    val locations: List<Location>,
)
