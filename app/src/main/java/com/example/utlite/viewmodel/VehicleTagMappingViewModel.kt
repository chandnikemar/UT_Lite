package com.example.utlite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.Utils
import com.example.utlite.model.vehicletagmapping.RfidMappingModel
import com.example.utlite.model.vehicletagmapping.RfidMappingResultModel
import com.example.utlite.model.vehicletracking.TrackVehicleModel
import com.example.utlite.model.vehicletracking.TrackVehicleResultModel
import com.example.utlite.repository.UtLiteRepository
import com.google.gson.Gson
import com.kemarport.hindalco.model.LoginRequest
import com.kemarport.hindalco.model.LoginResponse
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class VehicleTagMappingViewModel (
    application: Application,
    private val utLiteRepository: UtLiteRepository
) : AndroidViewModel(application) {
    val  rfidMappingMutableLiveData: MutableLiveData<Resource<RfidMappingResultModel>> = MutableLiveData()

    fun rfidMapping(
        token: String,
        baseUrl: String,
        rfidMappingModel: RfidMappingModel
    ) {
        viewModelScope.launch {
            safeAPICallGetTrackVehicleDetails(token, baseUrl, rfidMappingModel)
        }
    }

    private suspend fun safeAPICallGetTrackVehicleDetails(
        token: String,
        baseUrl: String,
        rfidMappingModel: RfidMappingModel
    ) {
        rfidMappingMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    utLiteRepository.rfidMapping (token, baseUrl, rfidMappingModel)
                rfidMappingMutableLiveData.postValue(handleApiCallRfidMapping(response))
            } else {
                rfidMappingMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    rfidMappingMutableLiveData.postValue(Resource.Error("${t.message}"))
                }

                else -> rfidMappingMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleApiCallRfidMapping(response: Response<RfidMappingResultModel>): Resource<RfidMappingResultModel> {
        return if (response.isSuccessful) {
            response.body()?.let { body ->
                if (body.status.equals("Success", ignoreCase = true)) {
                    Resource.Success(body)
                } else {
                    Resource.Error(body.statusMessage ?: "Unknown error", body)
                }
            } ?: Resource.Error("Response body is null")
        } else {
            response.errorBody()?.let {
                try {
                    val errorObject = JSONObject(it.charStream().readText())
                    val gson = Gson()
                    val parsedModel = gson.fromJson(errorObject.toString(), RfidMappingResultModel::class.java)
                    val errorMessage = errorObject.optString("statusMessage", "Unknown error")
                    return Resource.Error(errorMessage, parsedModel)
                } catch (e: Exception) {
                    return Resource.Error("Error parsing response")
                }
            } ?: Resource.Error("Unknown error")
        }
    }
}