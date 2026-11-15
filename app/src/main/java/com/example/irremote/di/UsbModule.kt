package com.example.irremote.di

import android.content.Context
import com.example.irremote.usb.UsbSerialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UsbModule {

    @Provides
    @Singleton
    fun provideUsbSerialManager(@ApplicationContext context: Context): UsbSerialManager {
        return UsbSerialManager(context)
    }
}