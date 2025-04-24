package com.example.utlite.model.vehicledetection

data class Location (
    val locationId: Int,
    val locationName: String,
    val locationCode: String,
    val parentLocationCode: String,
    val locationType: String,
    val sequence: Int,
    val detectableBy: String,
    val isActive: Boolean,
    val displayName: String,
    val maxQueueSize: Int,
    val minQueueSize: Int,
    val createdBy: String,
    val createdDate: String,
    val modifiedBy: String,
    val modifiedDate: String,
    val weighBridgeMaster: List<Any>,
    val currentQueue: List<Any>
)