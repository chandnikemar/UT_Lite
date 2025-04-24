package com.example.utlite.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.utlite.repository.UtLiteRepository

class VehicleDetectionViewModelFactory (
    private val application: Application,
    private val utLiteRepository: UtLiteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return VehicleDetectionViewModel(application, utLiteRepository) as T
    }
}