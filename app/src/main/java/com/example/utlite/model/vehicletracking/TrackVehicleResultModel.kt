package com.example.utlite.model.vehicletracking

data class TrackVehicleResultModel(
    val requestId: String,
    val status: String,
    val statusMessage: String,
    val vehicleTransactionDetails: VehicleTransactionDetails
)