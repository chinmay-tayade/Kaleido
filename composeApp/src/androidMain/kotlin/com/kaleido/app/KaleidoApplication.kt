package com.kaleido.app

import android.app.Application
import com.kaleido.app.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class KaleidoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@KaleidoApplication)
        }
    }
}
