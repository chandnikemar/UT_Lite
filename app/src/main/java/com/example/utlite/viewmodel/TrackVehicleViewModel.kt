package com.example.utlite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.Utils
import com.example.utlite.model.vehicletracking.TrackVehicleModel
import com.example.utlite.model.vehicletracking.TrackVehicleResultModel
import com.example.utlite.repository.UtLiteRepository

import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response

class TrackVehicleViewModel (
    application: Application,
    private val utLiteRepository: UtLiteRepository
) : AndroidViewModel(application) {
    val  trackVehicleDetailsMutableLiveData: MutableLiveData<Resource<TrackVehicleResultModel>> = MutableLiveData()

    fun getTrackVehicleDetails(
        token: String,
        baseUrl: String,
        trackVehicleModel: TrackVehicleModel,
    ) {
        viewModelScope.launch {
            safeAPICallGetTrackVehicleDetails(token, baseUrl, trackVehicleModel)
        }
    }


    private suspend fun safeAPICallGetTrackVehicleDetails(
        token: String,
        baseUrl: String,
        trackVehicleModel: TrackVehicleModel,
    ) {
        trackVehicleDetailsMutableLiveData.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val response =
                    utLiteRepository.getTrackVehicleDetails(token, baseUrl, trackVehicleModel)
                trackVehicleDetailsMutableLiveData.postValue(handleGetTrackVehicleDetails(response))
            } else {
                trackVehicleDetailsMutableLiveData.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is Exception -> {
                    trackVehicleDetailsMutableLiveData.postValue(Resource.Error("${t.message}"))
                }
                else -> trackVehicleDetailsMutableLiveData.postValue(Resource.Error(Constants.CONFIG_ERROR))
            }
        }
    }

    private fun handleGetTrackVehicleDetails(response: Response<TrackVehicleResultModel>): Resource<TrackVehicleResultModel> {
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