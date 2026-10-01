package com.example.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.data.network.ExternalIntegrationsClient

class AppContainer(private val application: Application) {
    val database: AppDatabase by lazy {
        val builder = if (System.getProperty("robolectric.active") != null) {
            Room.inMemoryDatabaseBuilder(application, AppDatabase::class.java)
                .allowMainThreadQueries()
        } else {
            Room.databaseBuilder(application, AppDatabase::class.java, "creator_coop.db")
        }
        builder
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    
    val externalClient: ExternalIntegrationsClient by lazy {
        ExternalIntegrationsClient()
    }

    val repository: AppRepository by lazy {
        AppRepository(database, application, externalClient)
    }
    
    val sharedPreferences: SharedPreferences by lazy {
        application.getSharedPreferences("creator_coop_prefs", Context.MODE_PRIVATE)
    }
}
