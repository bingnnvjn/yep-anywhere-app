package com.yepanywhere.app

import android.app.Application
import com.yepanywhere.app.data.SettingsDataStore

class YepApplication : Application() {
    lateinit var settingsDataStore: SettingsDataStore
        private set

    override fun onCreate() {
        super.onCreate()
        settingsDataStore = SettingsDataStore(this)
    }
}
