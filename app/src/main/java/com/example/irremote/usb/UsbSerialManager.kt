package com.example.irremote.usb

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsbSerialManager @Inject constructor(
    @ApplicationContext private val context: Context
) : SerialInputOutputManager.Listener {

    companion object {
        const val ACTION_USB_PERMISSION = "com.example.irremote.USB_PERMISSION"
        private const val BAUD_RATE = 115200
    }

    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var serialPort: UsbSerialPort? = null
    private var ioManager: SerialInputOutputManager? = null

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _receivedSignals = MutableSharedFlow<IrSignal>()
    val receivedSignals = _receivedSignals.asSharedFlow()

    private var buffer = java.lang.StringBuilder()
    private var isReadingRawArray = false

    fun connect() {
        if (_isConnected.value) return

        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
        if (availableDrivers.isEmpty()) {
            return // No drivers found (could emit an error state here)
        }

        val driver = availableDrivers[0]
        val device = driver.device

        if (usbManager.hasPermission(device)) {
            openPort(driver.ports[0])
        } else {
            val flags = PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            val permissionIntent = PendingIntent.getBroadcast(
                context, 0, Intent(ACTION_USB_PERMISSION), flags
            )
            usbManager.requestPermission(device, permissionIntent)
        }
    }

    fun handlePermissionResult(device: UsbDevice?, granted: Boolean) {
        if (granted && device != null) {
            val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
            driver?.let { openPort(it.ports[0]) }
        }
    }

    private fun openPort(port: UsbSerialPort) {
        try {
            val connection = usbManager.openDevice(port.device) ?: return
            port.open(connection)
            port.setParameters(
                BAUD_RATE,
                8,
                UsbSerialPort.STOPBITS_1,
                UsbSerialPort.PARITY_NONE
            )
            serialPort = port
            ioManager = SerialInputOutputManager(port, this).apply { start() }
            _isConnected.value = true
        } catch (e: Exception) {
            disconnect()
        }
    }

    fun disconnect() {
        ioManager?.apply {
            listener = null
            stop()
        }
        ioManager = null
        try {
            serialPort?.close()
        } catch (e: Exception) {
            // Ignore close errors
        }
        serialPort = null
        _isConnected.value = false
    }

    fun sendCommand(command: String) {
        try {
            val data = if (command.endsWith("\n")) command else "$command\n"
            serialPort?.write(data.toByteArray(), 1000)
        } catch (e: Exception) {
            disconnect()
        }
    }

    override fun onNewData(data: ByteArray?) {
        data?.let {
            val text = String(it)
            buffer.append(text)
            processBuffer()
        }
    }

    override fun onRunError(e: Exception?) {
        managerScope.launch { disconnect() }
    }

    private fun processBuffer() {
        var newlineIndex = buffer.indexOf('\n')
        while (newlineIndex != -1) {
            val line = buffer.substring(0, newlineIndex).trim()
            buffer.delete(0, newlineIndex + 1)
            handleLine(line)
            newlineIndex = buffer.indexOf('\n')
        }
    }

    private fun handleLine(line: String) {
        if (line.startsWith("RECV:NEC:")) {
            // Expected format: RECV:NEC:<addressHex>:<commandHex>
            val parts = line.split(":")
            if (parts.size >= 4) {
                val address = parts[2]
                val command = parts[3]
                managerScope.launch {
                    _receivedSignals.emit(IrSignal.Nec(address, command))
                }
            }
        } else if (line.startsWith("RECV:RAW_ARRAY")) {
            isReadingRawArray = true
        } else if (isReadingRawArray && line.contains("uint16_t rawData")) {
            // Expected format: uint16_t rawData[x] = {8950, 4450, ...};
            val startIndex = line.indexOf('{')
            val endIndex = line.indexOf('}')
            if (startIndex != -1 && endIndex != -1) {
                val rawDataStr = line.substring(startIndex + 1, endIndex).replace(" ", "")
                managerScope.launch {
                    _receivedSignals.emit(IrSignal.Raw(rawDataStr))
                }
            }
            isReadingRawArray = false
        }
    }
}