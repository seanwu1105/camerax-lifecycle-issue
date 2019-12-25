package io.numbers.cameraxlifecycleissue

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import timber.log.Timber

class CameraObserver : DefaultLifecycleObserver {

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        Timber.i("onCreate")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Timber.i("onStart")
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        Timber.i("onResume")
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        Timber.i("onPause")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Timber.i("onStop")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        Timber.i("onDestroy")
    }
}