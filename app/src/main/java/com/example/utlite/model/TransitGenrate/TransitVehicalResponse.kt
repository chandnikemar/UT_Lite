package com.example.utlite.model.TransitGenrate


data class TransitVehicalResponse(
    val statusCode: Int,
    val errorMessage: String,
    val exception: String?,
    val responseMessage: String?
)
