package com.example.utlite.viewmodel.transitPass

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.example.utlite.repository.UtLiteRepository


class transitPassViewModelFactory
    (
    private val application: Application,
    private val utLiteRepository: UtLiteRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TransitPassViewModel(application, utLiteRepository) as T
    }}
