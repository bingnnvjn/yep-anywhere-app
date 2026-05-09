package com.yepanywhere.app

import android.app.Application
import com.yepanywhere.app.data.SettingsStore

class YepApplication : Application() {
    lateinit var settingsStore: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        settingsStore = SettingsStore(this)
    }
}
