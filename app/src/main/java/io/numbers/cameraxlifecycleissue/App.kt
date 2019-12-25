package io.numbers.cameraxlifecycleissue

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import timber.log.Timber

class App : Application(), CameraXConfig.Provider {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }

    override fun getCameraXConfig() = Camera2Config.defaultConfig()
}