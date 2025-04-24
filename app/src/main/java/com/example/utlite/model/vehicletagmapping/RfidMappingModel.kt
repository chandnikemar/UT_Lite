package com.example.utlite.model.vehicletagmapping

data class RfidMappingModel(
    val RequestId: String,
    val VRN: String,
    val RFIDTagNo: String,
    val ForceMap: String
)