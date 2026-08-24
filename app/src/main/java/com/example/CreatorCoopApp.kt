package com.example

import android.app.Application
import com.example.di.AppContainer
import com.example.ui.util.NotificationHelper
import com.example.util.PlatformExceptionGuard

class CreatorCoopApp : Application() {
    lateinit var container: AppContainer
    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.initNotificationChannels(this)
        PlatformExceptionGuard.initialize(container.repository)
    }
}
