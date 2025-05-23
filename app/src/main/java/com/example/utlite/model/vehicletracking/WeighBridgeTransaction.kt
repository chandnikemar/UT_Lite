package com.example.utlite.model.vehicletracking

data class WeighBridgeTransaction(
    val weighBridgeTransactionId: Int?,
    val jobMilestoneId: Int?,
    val weighBridgeId: Int?,
    val actualTareweight: Any?,
    val actualWeight: Double?,
    val type: Any?,
    val transactionDateTime: String?,
    val isImageCaptured: Boolean?,
    val status: String?
)