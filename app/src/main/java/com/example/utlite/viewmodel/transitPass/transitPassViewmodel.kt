package com.example.utlite.viewmodel.transitPass

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope


import com.example.utlite.model.TransitGenrate.QrTransitPassResponse
import com.example.utlite.model.TransitGenrate.TransitVehicalRequest
import com.example.utlite.model.TransitGenrate.TransitVehicalResponse
import com.example.utlite.model.TransitGenrate.QrTransitPassRequest

import com.example.utlite.helper.Constants
import com.example.utlite.helper.Resource
import com.example.utlite.helper.Utils
import com.example.utlite.model.TransitPassResponse

import com.example.utlite.repository.UtLiteRepository
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

class TransitPassViewModel(
    application: Application,
    private val utLiteRepository: UtLiteRepository
) : AndroidViewModel(application) {

    // MutableLiveData to handle TransitPass data
    val transitPassResponseMutable: MutableLiveData<Resource<TransitPassResponse>> =
        MutableLiveData()

    val transitPassCreateResponseMutable: MutableLiveData<Resource<TransitVehicalResponse>> =
        MutableLiveData()


    // Function to call the API and get TransitPass details
    fun getTransitPassDetails(token: String, baseUrl: String, tpNumber: Int) {
        viewModelScope.launch {
            safeAPICallGetTransitPassDetails(token, baseUrl, tpNumber)
        }
    }

    fun createTransitPass(
        token: String,
        baseUrl: String,

        transitVehicalRequest: TransitVehicalRequest
    ) {
        viewModelScope.launch {
            safeAPICallCreateTransitPass(token, baseUrl, transitVehicalRequest)
        }
    }



    // Safe API call to fetch TransitPass details
    private suspend fun safeAPICallGetTransitPassDetails(
        token: String,
        baseUrl: String,
        tpNumber: Int
    ) {
        transitPassResponseMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                // Construct the URL
                val requestUrl = "$baseUrl${Constants.Get_Transit_DetailsB_TPNo}?TPNumber=$tpNumber"
                Log.d("API_Request", "Request URL: $requestUrl")
                Log.d("API_Request", "Token: $token")

                // Make the API call
                val response = utLiteRepository.getTransitPassDetails(token,baseUrl, tpNumber )

                // Handle response
                transitPassResponseMutable.postValue(handleGetTransitPassDetails(response))
            } else {
                transitPassResponseMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> transitPassResponseMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> transitPassResponseMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }

    // Handling the API response for TransitPass details
    private fun handleGetTransitPassDetails(response: Response<TransitPassResponse>): Resource<TransitPassResponse> {
        var errorMessage = ""
        if (response.isSuccessful) {
            response.body()?.let { transitPassResponse ->
                return Resource.Success(transitPassResponse)
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

    // Create Transit Pass
    private suspend fun safeAPICallCreateTransitPass(
        token: String,
        baseUrl: String,
        transitVehicalRequest: TransitVehicalRequest
    ) {
        transitPassCreateResponseMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val requestUrl = "$baseUrl${Constants.POST_CreateVehicl_TransactionBy_TPDetails}"
                val response =
                    utLiteRepository.createVehicleTransit(token, baseUrl , transitVehicalRequest)

                // Log the response for debugging
                Log.d("API_Response", "Create Transit Response: ${response}")
                Log.d("API_Request", "Create Transit Request: ${requestUrl}")

                // Handle response
                transitPassCreateResponseMutable.postValue(handleCreateTransitPassResponse(response))
            } else {
                transitPassCreateResponseMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> transitPassCreateResponseMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> transitPassCreateResponseMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }

    // Handling the API response for creating a TransitPass
    private fun handleCreateTransitPassResponse(response: Response<TransitVehicalResponse>): Resource<TransitVehicalResponse> {
        var errorMessage = ""

        if (response.isSuccessful) {
            response.body()?.let { response ->
                return Resource.Success(response)
            }
        } else if (response.errorBody() != null) {





        }

        return Resource.Error(errorMessage)
    }
////////////////////////// QR CODE Odisha
// MutableLiveData to handle QR code Transit Pass data
val qrTransitPassResponseMutable: MutableLiveData<Resource<QrTransitPassResponse>> = MutableLiveData()

    // Function to call the API to add Transit Pass by QR Code
    fun addTransitPassByQRCode(
        token: String,
        baseUrl: String,
        qrTransitPassRequest: QrTransitPassRequest
    ) {
        viewModelScope.launch {
            safeAPICallAddTransitPassByQRCode(token, baseUrl, qrTransitPassRequest)
        }
    }

    // Safe API call to add Transit Pass by QR Code
    private suspend fun safeAPICallAddTransitPassByQRCode(
        token: String,
        baseUrl: String,
        qrTransitPassRequest: QrTransitPassRequest
    ) {
        qrTransitPassResponseMutable.postValue(Resource.Loading())
        try {
            if (Utils.hasInternetConnection(getApplication())) {
                val requestUrl = "$baseUrl${Constants.POST_AddTransitPassDetailsBy_QRCode}"
                Log.d("API_Request", "Request URL: $requestUrl")
                Log.d("API_Request", "Token: $token")
                Log.d("API_Request", "Request Body: $qrTransitPassRequest")

                // Make the API call
                val response = utLiteRepository.addTransitPassByQRCode(token, baseUrl,qrTransitPassRequest)

                // Handle the response
                qrTransitPassResponseMutable.postValue(handleAddTransitPassByQRCodeResponse(response))
            } else {
                qrTransitPassResponseMutable.postValue(Resource.Error(Constants.NO_INTERNET))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> qrTransitPassResponseMutable.postValue(Resource.Error(Constants.CONFIG_ERROR))
                else -> qrTransitPassResponseMutable.postValue(Resource.Error("${t.message}"))
            }
        }
    }

    // Handling the API response for adding Transit Pass by QR Code
    private fun handleAddTransitPassByQRCodeResponse(response: Response<QrTransitPassResponse>): Resource<QrTransitPassResponse> {
        var errorMessage = ""

        if (response.isSuccessful) {
            response.body()?.let { response ->

                return Resource.Success(response)
                Log.d("API_Response", "API call success: ${response.responseMessage}")
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


}
