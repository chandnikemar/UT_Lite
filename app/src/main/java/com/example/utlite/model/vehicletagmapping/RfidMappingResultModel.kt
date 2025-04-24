package com.example.utlite.model.vehicletagmapping

data class RfidMappingResultModel(
    val vrn: String,
    val status: String,
    val statusMessage: String,
    val requestId: String
)
