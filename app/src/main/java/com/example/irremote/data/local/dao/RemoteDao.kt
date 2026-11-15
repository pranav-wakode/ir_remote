package com.example.irremote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.irremote.data.local.entity.RemoteButton
import com.example.irremote.data.local.entity.RemoteDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: RemoteDevice): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: RemoteButton): Long

    @Query("SELECT * FROM remote_devices")
    fun getAllDevices(): Flow<List<RemoteDevice>>

    @Query("SELECT * FROM remote_buttons WHERE deviceId = :deviceId")
    fun getButtonsForDevice(deviceId: Int): Flow<List<RemoteButton>>
}