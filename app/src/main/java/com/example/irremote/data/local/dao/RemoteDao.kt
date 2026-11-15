package com.example.irremote.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.irremote.data.local.entity.RemoteButton
import com.example.irremote.data.local.entity.RemoteDevice
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: RemoteDevice): Long

    @Update
    suspend fun updateDevice(device: RemoteDevice)

    @Delete
    suspend fun deleteDevice(device: RemoteDevice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButton(button: RemoteButton): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertButtons(buttons: List<RemoteButton>)

    @Update
    suspend fun updateButton(button: RemoteButton)

    @Delete
    suspend fun deleteButton(button: RemoteButton)

    @Query("SELECT * FROM remote_devices")
    fun getAllDevices(): Flow<List<RemoteDevice>>

    @Query("SELECT * FROM remote_buttons WHERE deviceId = :deviceId ORDER BY id ASC")
    fun getButtonsForDevice(deviceId: Int): Flow<List<RemoteButton>>
}