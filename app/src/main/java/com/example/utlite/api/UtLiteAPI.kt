package com.example.utlite.api


import com.example.utlite.model.TransitGenrate.QrTransitPassResponse
import com.example.utlite.model.TransitGenrate.TransitVehicalRequest
import com.example.utlite.model.TransitGenrate.TransitVehicalResponse
import com.example.utlite.model.TransitGenrate.QrTransitPassRequest
import com.example.utlite.helper.Constants
import com.example.utlite.model.TransitPassResponse
import com.example.utlite.model.vehicledetection.GetLocationListResponse
import com.example.utlite.model.vehicledetection.GetLocationMasterDataByLocationIdResponse
import com.example.utlite.model.vehicledetection.PostRfidModel
import com.example.utlite.model.vehicledetection.PostRfidResultModel
import com.example.utlite.model.vehicletagmapping.RfidMappingModel
import com.example.utlite.model.vehicletagmapping.RfidMappingResultModel
import com.example.utlite.model.vehicletracking.TrackVehicleModel
import com.example.utlite.model.vehicletracking.TrackVehicleResultModel
import com.kemarport.hindalco.model.LoginRequest
import com.kemarport.hindalco.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query


interface UtLiteAPI {

    //login
    @POST(Constants.LOGIN_URL)
    suspend fun login(@Body loginRequest: LoginRequest, ): Response<LoginResponse>


    //vehicle detection page
    @GET(Constants.getLocationList)
    suspend fun getVehicleLocationList(
        @Header("Authorization") jwtToken: String,
        @Query("RequestId") requestId: Int,
        @Query("ParentLocationCode") parentLocationCode: String
    ): Response<GetLocationListResponse>

    @GET(Constants.getLocationMasterDataByLocationId)
    suspend fun getLocationMasterDataByLocationId(
        @Header("Authorization") token: String,
        @Query("RequestId") requestId: Int,
        @Query("LocationId") locationId: Int
    ): Response<ArrayList<GetLocationMasterDataByLocationIdResponse>>

    @POST(Constants.pOSTRFIDTag)
    suspend fun postRfid(
        @Header("Authorization") jwtToken: String,
        @Body postRfidModel: PostRfidModel
    ): Response<PostRfidResultModel>

    //vehicle tag mapping page
    @POST(Constants.postRFIDVerifyMap)
    suspend fun rfidMapping(
        @Header("Authorization") jwtToken: String,
        @Body rfidMappingModel: RfidMappingModel
    ): Response<RfidMappingResultModel>

    //vehicle track page

    @POST(Constants.postVehicleTrackingRequest)
    suspend fun getTrackVehicleDetails(
        @Header("Authorization") jwtToken: String,
        @Body trackVehicleModel: TrackVehicleModel
    ): Response<TrackVehicleResultModel>
    ////TP
    @GET(Constants.Get_Transit_DetailsB_TPNo)
    suspend fun getTransitDetailINfo(
        @Header(Constants.HTTP_HEADER_AUTHORIZATION) bearerToken: String,
        @Query("TPNumber")tpNO: Int,

        ): Response<TransitPassResponse>


    @POST(Constants.POST_CreateVehicl_TransactionBy_TPDetails)
    suspend fun createVehicleTransit(
        @Header("Authorization") token: String,

        @Body transitVehicalRequest: TransitVehicalRequest
    ): Response<TransitVehicalResponse>

    @POST(Constants.POST_AddTransitPassDetailsBy_QRCode)
    suspend fun addTransitPassByQRCode(
        @Header("Authorization") token: String,
        @Body qrTransitPassRequest: QrTransitPassRequest
    ): Response<QrTransitPassResponse>

}