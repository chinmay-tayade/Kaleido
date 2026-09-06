package com.example.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry point. [HiltAndroidApp] triggers Hilt's code generation and
 * makes the [SingletonComponent] container available to the whole app.
 */
@HiltAndroidApp
class StarterApp : Application()
