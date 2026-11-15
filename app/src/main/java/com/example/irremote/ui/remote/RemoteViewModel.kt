package com.example.irremote.ui.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.irremote.data.local.entity.RemoteButton
import com.example.irremote.repository.RemoteRepository
import com.example.irremote.usb.IrSignal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemoteViewModel @Inject constructor(
    private val repository: RemoteRepository
) : ViewModel() {

    private var currentDeviceId: Int = -1

    private val _buttons = MutableStateFlow<List<RemoteButton>>(emptyList())
    val buttons = _buttons.asStateFlow()

    private val _learningSignal = MutableStateFlow<IrSignal?>(null)
    val learningSignal = _learningSignal.asStateFlow()

    private var buttonsJob: Job? = null
    private var signalJob: Job? = null

    fun loadDevice(deviceId: Int) {
        currentDeviceId = deviceId
        buttonsJob?.cancel()
        buttonsJob = viewModelScope.launch {
            repository.getButtonsForDevice(deviceId).collect { buttonList ->
                _buttons.value = buttonList
            }
        }
    }

    fun sendButtonSignal(button: RemoteButton) {
        val command = when (button.protocol) {
            "NEC" -> "SEND:NEC:${button.addressHex}:${button.commandHex}"
            "RAW" -> "SEND:RAW:${button.rawArrayData}"
            else -> return
        }
        repository.sendCommand(command)
    }

    fun enterLearningMode() {
        _learningSignal.value = null
        signalJob?.cancel()
        signalJob = viewModelScope.launch {
            repository.receivedSignals.collect { signal ->
                _learningSignal.value = signal
            }
        }
    }

    fun exitLearningMode() {
        signalJob?.cancel()
        _learningSignal.value = null
    }

    fun saveLearnedButton(label: String) {
        val signal = _learningSignal.value ?: return

        viewModelScope.launch {
            val button = when (signal) {
                is IrSignal.Nec -> RemoteButton(
                    deviceId = currentDeviceId,
                    buttonLabel = label,
                    protocol = "NEC",
                    addressHex = signal.addressHex,
                    commandHex = signal.commandHex,
                    rawArrayData = null
                )
                is IrSignal.Raw -> RemoteButton(
                    deviceId = currentDeviceId,
                    buttonLabel = label,
                    protocol = "RAW",
                    addressHex = null,
                    commandHex = null,
                    rawArrayData = signal.rawArray
                )
            }
            repository.insertButton(button)
            exitLearningMode()
        }
    }

    fun renameButton(button: RemoteButton, newLabel: String) {
        viewModelScope.launch {
            repository.updateButton(button.copy(buttonLabel = newLabel))
        }
    }

    fun deleteButton(button: RemoteButton) {
        viewModelScope.launch {
            repository.deleteButton(button)
        }
    }
}