package com.example.irremote.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.irremote.data.local.entity.RemoteDevice
import com.example.irremote.repository.RemoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: RemoteRepository
) : ViewModel() {

    val isUsbConnected = repository.isUsbConnected
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val devices = repository.getAllDevices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun connectUsb() {
        repository.connectUsb()
    }

    fun addPreloadedDevice() {
        viewModelScope.launch {
            val samsungTv = RemoteDevice(
                deviceName = "Generic Samsung TV",
                deviceType = "TV"
            )
            repository.insertDevice(samsungTv)
            // In a full implementation, you might pre-populate standard NEC buttons here as well.
        }
    }
}