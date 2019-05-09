package com.example.smackchat.Controller

import android.app.Application
import com.example.smackchat.Utilities.SharedPrefs

class App:Application() {

    companion object{
        lateinit var prefs: SharedPrefs
    }

    override fun onCreate() {
        prefs = SharedPrefs(applicationContext)
        super.onCreate()

    }
}