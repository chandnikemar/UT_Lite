package com.example.utlite.model.TransitGenrate

data class QrTransitPassRequest(
    val transitPassNo: String,
    val vrn: String,
    val driverDetails: String?,
    val transporterDetails: String,
    val tareWeight: String,
    val grossWeight: String,
    val netWeight: String,
    val mineralName: String,
    val grade: String
)