package com.example.irremote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_devices")
data class RemoteDevice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceName: String,
    val deviceType: String // e.g., "TV", "AC", "Fan"
)