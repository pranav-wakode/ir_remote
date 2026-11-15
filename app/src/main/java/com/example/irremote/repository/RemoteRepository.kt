package com.example.irremote.repository

import com.example.irremote.data.local.dao.RemoteDao
import com.example.irremote.data.local.entity.RemoteButton
import com.example.irremote.data.local.entity.RemoteDevice
import com.example.irremote.usb.IrSignal
import com.example.irremote.usb.UsbSerialManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteRepository @Inject constructor(
    private val remoteDao: RemoteDao,
    private val usbManager: UsbSerialManager
) {
    val isUsbConnected: Flow<Boolean> = usbManager.isConnected
    val receivedSignals: Flow<IrSignal> = usbManager.receivedSignals

    fun connectUsb() = usbManager.connect()
    fun disconnectUsb() = usbManager.disconnect()
    fun sendCommand(command: String) = usbManager.sendCommand(command)

    fun getAllDevices(): Flow<List<RemoteDevice>> = remoteDao.getAllDevices()
    fun getButtonsForDevice(deviceId: Int): Flow<List<RemoteButton>> = remoteDao.getButtonsForDevice(deviceId)

    suspend fun insertDevice(device: RemoteDevice): Long = remoteDao.insertDevice(device)
    suspend fun updateDevice(device: RemoteDevice) = remoteDao.updateDevice(device)
    suspend fun deleteDevice(device: RemoteDevice) = remoteDao.deleteDevice(device)

    suspend fun insertButton(button: RemoteButton): Long = remoteDao.insertButton(button)
    suspend fun insertButtons(buttons: List<RemoteButton>) = remoteDao.insertButtons(buttons)
    suspend fun updateButton(button: RemoteButton) = remoteDao.updateButton(button)
    suspend fun deleteButton(button: RemoteButton) = remoteDao.deleteButton(button)
}