package com.nullo.voidapp

import android.app.Application
import com.nullo.voidapp.di.initKoin
import org.koin.android.ext.koin.androidContext

class VoidApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(this@VoidApp)
        }
    }
}