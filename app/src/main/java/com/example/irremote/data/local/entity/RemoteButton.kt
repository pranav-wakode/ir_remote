package com.example.irremote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "remote_buttons",
    foreignKeys = [
        ForeignKey(
            entity = RemoteDevice::class,
            parentColumns = ["id"],
            childColumns = ["deviceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("deviceId")]
)
data class RemoteButton(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceId: Int,
    val buttonLabel: String,
    val protocol: String, // "NEC" or "RAW"
    val addressHex: String?,
    val commandHex: String?,
    val rawArrayData: String?
)