package com.example.utlite.model

data class TransitPassResponse(
    val transitPassDetailsResponse: TransitPassDetails, // Updated to match API response key
    val vrn: String,
    val rfidTagNo: String?,
    val isVRNRegistered: Boolean,
    val isRFIDTagMapped: Boolean,
    val statusCode: Int,
    val errorMessage: String?,
    val exception: String?,
    val responseMessage: String?
)

data class TransitPassDetails(
    val transitPassId: Int,
    val transitPassNo: String,
    val vrn: String,
    val driverDetails: String,
    val transporterDetails: String,
    val tareWeight: Int,
    val grossWeight: Int,
    val netWeight: Int,
    val mineralName: String,
    val grade: String,
    val transitPassType: String,
    val status: String,
    val isActive: Boolean,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String?,
    val modifiedDate: String?
)
