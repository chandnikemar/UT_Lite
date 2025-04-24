package com.example.utlite.repository


import com.example.utlite.model.TransitGenrate.QrTransitPassResponse
import com.example.utlite.model.TransitGenrate.TransitVehicalRequest
import com.example.utlite.model.TransitGenrate.TransitVehicalResponse
import com.example.utlite.model.TransitGenrate.QrTransitPassRequest
import com.example.utlite.api.RetrofitInstance
import com.example.utlite.model.TransitPassResponse
import com.example.utlite.model.vehicledetection.PostRfidModel
import com.example.utlite.model.vehicletagmapping.RfidMappingModel
import com.example.utlite.model.vehicletracking.TrackVehicleModel
import com.kemarport.hindalco.model.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Query


class UtLiteRepository {

    suspend fun login(
        baseUrl: String,
        @Body
        loginRequest: LoginRequest,
    ) = RetrofitInstance.api(baseUrl).login(loginRequest)

    //vehicle detection page

    suspend fun getVehicleLocationList(
        token: String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("ParentLocationCode") parentLocationCode: String
    ) = RetrofitInstance.api(baseUrl).getVehicleLocationList(token, requestId, parentLocationCode)

    suspend fun getLocationMasterDataByLocationId(
        token: String,
        baseUrl: String,
        @Query("RequestId") requestId: Int,
        @Query("LocationId") locationId: Int
    ) = RetrofitInstance.api(baseUrl)
        .getLocationMasterDataByLocationId(token, requestId, locationId)


    suspend fun postRfid(
        token: String,
        baseUrl: String,
        @Body postRfidModel: PostRfidModel
    ) = RetrofitInstance.api(baseUrl).postRfid(token, postRfidModel)

    suspend fun rfidMapping(
        token: String,
        baseUrl: String,
        @Body rfidMappingModel: RfidMappingModel
    ) = RetrofitInstance.api(baseUrl).rfidMapping(token, rfidMappingModel)

    suspend fun getTrackVehicleDetails(
        token: String,
        baseUrl: String,
        @Body trackVehicleModel: TrackVehicleModel
    ) = RetrofitInstance.api(baseUrl).getTrackVehicleDetails(token, trackVehicleModel)


    //TP
    suspend fun getTransitPassDetails(
        token: String,
        baseUrl: String,

        tpNumber: Int, // TPNumber is now a String


    ): Response<TransitPassResponse> {

        return RetrofitInstance.api(baseUrl).getTransitDetailINfo(token, tpNumber)
    }

    suspend fun createVehicleTransit(
        token: String,
        baseUrl: String,

        transitVehicalRequest: TransitVehicalRequest
    ): Response<TransitVehicalResponse> {
        return RetrofitInstance.api(baseUrl).createVehicleTransit(token, transitVehicalRequest)
    }

    suspend fun addTransitPassByQRCode(
        token: String,
        baseUrl: String,
        qrTransitPassRequest: QrTransitPassRequest,
    ): Response<QrTransitPassResponse> {
        return RetrofitInstance.api(baseUrl).addTransitPassByQRCode(token, qrTransitPassRequest)
    }
}