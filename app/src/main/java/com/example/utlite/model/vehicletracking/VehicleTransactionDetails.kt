package com.example.utlite.model.vehicletracking

data class VehicleTransactionDetails(
    val vehicleTransactionId: Int,
    val vehicleTransactionCode: String,
    val vrn: String,
    val driverId: Int,
    val rfidTagNumber: String,
    val tranType: Int,
    val shipmentNo: String,
    val gateEntryNo: String,
    val transactionDate: String,
    val transactionStartTime: Any?,
    val transactionEndTime: Any?,
    val isActive: Boolean,
    val tranStatus: String,
    val remarks: Any?,
    val driverName: String,
    val phoneNumber: String,
    val jobMilestones: List<JobMilestone>?
)
