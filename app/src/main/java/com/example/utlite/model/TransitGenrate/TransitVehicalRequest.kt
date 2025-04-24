package com.example.utlite.model.TransitGenrate


data class  TransitVehicalRequest(
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
    val modifiedDate: String?,

)
