package com.example.utlite.model.vehicledetection

data class PostRfidModel (
    val requestId: String,
    val RFIDTagNo: String,
    val devicelocationId: String,
    val VRN: String,
    val reason: String
)