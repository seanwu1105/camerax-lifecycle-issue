package io.numbers.cameraxlifecycleissue

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var cameraComponent: CameraComponent? = null
    private val cameraObserver = CameraObserver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        askPermission()

        cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cameraComponent = CameraComponent(this).apply {
                    onUpdatedListeners.add {
                        imageView.setImageBitmap(BitmapFactory.decodeByteArray(it, 0, it.size))
                    }
                    lifecycle.addObserver(cameraObserver)
                    onStart()
                }
            } else cameraComponent?.onStop()
        }

        destroyCameraButton.setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                cameraComponent?.apply {
                    //                    onStop()
//                    delay(1000)  // This delay could fix the problem. Thus, it seems like a race condition not handled.
                    onDestroy()
                }
            }
        }
    }

    private fun askPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) requestPermissions(arrayOf(Manifest.permission.CAMERA), 10)
    }
}
