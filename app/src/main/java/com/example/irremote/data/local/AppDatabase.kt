package com.example.irremote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.irremote.data.local.dao.RemoteDao
import com.example.irremote.data.local.entity.RemoteButton
import com.example.irremote.data.local.entity.RemoteDevice

@Database(
    entities = [RemoteDevice::class, RemoteButton::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun remoteDao(): RemoteDao
}