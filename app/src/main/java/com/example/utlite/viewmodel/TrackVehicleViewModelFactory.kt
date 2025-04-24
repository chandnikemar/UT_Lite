package com.example.utlite.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.repository.UtLiteRepository

class TrackVehicleViewModelFactory (
    private val application: Application,
    private val utLiteRepository: UtLiteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TrackVehicleViewModel(application, utLiteRepository) as T
    }
}