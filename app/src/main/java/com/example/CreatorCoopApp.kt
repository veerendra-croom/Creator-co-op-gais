package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.util.PlatformExceptionGuard

class CreatorCoopApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        PlatformExceptionGuard.initialize(container.repository)
    }
}
