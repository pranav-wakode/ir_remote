package com.example.irremote.di

import android.content.Context
import androidx.room.Room
import com.example.irremote.data.local.AppDatabase
import com.example.irremote.data.local.dao.RemoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "ir_remote_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideRemoteDao(appDatabase: AppDatabase): RemoteDao {
        return appDatabase.remoteDao()
    }
}