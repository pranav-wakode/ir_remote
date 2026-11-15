package com.example.irremote.usb

sealed class IrSignal {
    data class Nec(val addressHex: String, val commandHex: String) : IrSignal()
    data class Raw(val rawArray: String) : IrSignal() // comma-separated integers
}