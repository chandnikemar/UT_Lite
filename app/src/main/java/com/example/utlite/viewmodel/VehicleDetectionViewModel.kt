package com.example.utlite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.Utils
import com.example.utlite.model.vehicledetection.GetLocationListResponse
import com.example.utlite.model.vehicledetection.GetLocationMasterDataByLocationIdResponse
import com.example.utlite.model.vehicledetection.PostRfidModel
import com.example.utlite.model.vehicledetection.PostRfidResultModel
import com.example.utlite.model.vehicletracking.TrackVehicleModel
import com.example.utlite.model.vehicletracking.TrackVehicleResultModel
import com.example.utlite.repository.UtLiteRepository
import com.kemarport.hindalco.model.LoginRequest
import com.kemarport.hindalco.model.LoginResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class VehicleDetectionViewModel(
    application: Application,
    private val utLiteRepository: UtLiteRepository,
) : AndroidViewModel(application) {
    val vehicleLocationListMutableLiveData: MutableLiveData<Resource<GetLocationListResponse>> =
        MutableLiveData()

    fun getVehicleLocationList(
        token: String,
        baseUrl: String,
        requestId: Int,
        parentLocationCode: String,
    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleLocationList(token, baseUrl, requestId, parentLocationCode)
        }
    }


    private suspend fun safeAPICallGetVehicleLocationList(
        token: String,
        baseUrl: String,
        requestId: Int,
        parentLocationCode: String,
    ) {
        vehicleLocationListMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = utLiteRepository.getVehicleLocationList(
                    token, baseUrl, requestId, parentLocationCode
                )
                vehicleLocationListMutableLiveData.postValue(handleVehicleLocationList(response))
            } else {
                vehicleLocationListMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    vehicleLocationListMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

            }
        }
    }
    private fun handleVehicleLocationList(response: Response<GetLocationListResponse>): Resource<GetLocationListResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }
    val vehicleLocationListChild2MutableLiveData: MutableLiveData<Resource<GetLocationListResponse>> =
        MutableLiveData()

    fun getVehicleLocationListChild2(
        token: String,
        baseUrl: String,
        requestId: Int,
        parentLocationCode: String,
    ) {
        viewModelScope.launch {
            safeAPICallGetVehicleLocationListChild2(token, baseUrl, requestId, parentLocationCode)
        }
    }


    private suspend fun safeAPICallGetVehicleLocationListChild2(
        token: String,
        baseUrl: String,
        requestId: Int,
        parentLocationCode: String,
    ) {
        vehicleLocationListChild2MutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = utLiteRepository.getVehicleLocationList(
                    token, baseUrl, requestId, parentLocationCode
                )
                vehicleLocationListChild2MutableLiveData.postValue(handleVehicleLocationListChild2(response))
            } else {
                vehicleLocationListChild2MutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    vehicleLocationListChild2MutableLiveData.postValue(Resource.Error("${t.message}"))
                }

            }
        }
    }

    private fun handleVehicleLocationListChild2(response: Response<GetLocationListResponse>): Resource<GetLocationListResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }


    val getLocationMasterDataByLocationLiveData: MutableLiveData<Resource<ArrayList<GetLocationMasterDataByLocationIdResponse>>> =
        MutableLiveData()

    fun getLocationMasterDataByLocationId(
        token: String,
        baseUrl: String,
        requestId: Int,
        locationId: Int,
    ) {
        viewModelScope.launch {
            safeAPICallGetLocationMasterDataByLocationId(
                token, baseUrl, requestId, locationId
            )
        }
    }


    private suspend fun safeAPICallGetLocationMasterDataByLocationId(
        token: String,
        baseUrl: String,
        requestId: Int,
        locationId: Int,
    ) {
        getLocationMasterDataByLocationLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = utLiteRepository.getLocationMasterDataByLocationId(
                    token, baseUrl, requestId, locationId
                )
                getLocationMasterDataByLocationLiveData.postValue(
                    handleLocationMasterDataByLocationId(response)
                )
            } else {
                getLocationMasterDataByLocationLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    getLocationMasterDataByLocationLiveData.postValue(Resource.Error("${t.message}"))
                }

            }
        }
    }

    private fun handleLocationMasterDataByLocationId(response: Response<ArrayList<GetLocationMasterDataByLocationIdResponse>>): Resource<ArrayList<GetLocationMasterDataByLocationIdResponse>> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString(Constants.HTTP_ERROR_MESSAGE)
            }
        }
        return Resource.Error(errorMessage)
    }


    val postRfidLiveData: MutableLiveData<Resource<PostRfidResultModel>> =
        MutableLiveData()

    fun postRfid(
        token: String,
        baseUrl: String,
        postRfidModel: PostRfidModel
    ) {
        viewModelScope.launch { safeAPICallPostRfid(token, baseUrl, postRfidModel) }
    }


    private suspend fun safeAPICallPostRfid(
        token: String,
        baseUrl: String,
        postRfidModel: PostRfidModel
    ) {
        postRfidLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response = utLiteRepository.postRfid(token, baseUrl, postRfidModel)
                postRfidLiveData.postValue(handlePostRfid(response))
            } else {
                postRfidLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    postRfidLiveData.postValue(Resource.Error("${t.message}"))
                }

            }
        }
    }

    private fun handlePostRfid(response: Response<PostRfidResultModel>): Resource<PostRfidResultModel> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { Response ->
                return Resource.Success(Response)
            }
        } else if (response.errorBody() != null) {
            val errorObject = response.errorBody()?.let {
                JSONObject(it.charStream().readText())
            }
            errorObject?.let {
                errorMessage = it.getString("statusMessage")
            }
        }
        return Resource.Error(errorMessage)
    }


}