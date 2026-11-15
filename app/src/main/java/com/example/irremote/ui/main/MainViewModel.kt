package com.example.irremote.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.irremote.data.local.ReadymadeRemotes
import com.example.irremote.data.local.entity.RemoteButton
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

    fun addBlankDevice(name: String, type: String) {
        viewModelScope.launch {
            repository.insertDevice(RemoteDevice(deviceName = name, deviceType = type))
        }
    }

    fun addReadymadeDevice(template: ReadymadeRemotes.Template) {
        viewModelScope.launch {
            val deviceId = repository.insertDevice(
                RemoteDevice(deviceName = template.name, deviceType = template.type)
            ).toInt()

            val buttons = template.buttons.map { def ->
                RemoteButton(
                    deviceId = deviceId,
                    buttonLabel = def.label,
                    protocol = "NEC",
                    addressHex = def.address,
                    commandHex = def.command,
                    rawArrayData = null
                )
            }
            repository.insertButtons(buttons)
        }
    }

    fun renameDevice(device: RemoteDevice, newName: String) {
        viewModelScope.launch {
            repository.updateDevice(device.copy(deviceName = newName))
        }
    }

    fun deleteDevice(device: RemoteDevice) {
        viewModelScope.launch {
            repository.deleteDevice(device)
        }
    }
}