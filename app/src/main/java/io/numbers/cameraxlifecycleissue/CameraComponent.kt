package io.numbers.cameraxlifecycleissue

import android.content.Context
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

class CameraComponent(
    private val context: Context
) : LifecycleOwner {

    private var lifecycle = LifecycleRegistry(this)
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    val onUpdatedListeners = mutableListOf<(ByteArray) -> Unit>()

    init {
        lifecycle.currentState = Lifecycle.State.INITIALIZED
    }

    fun onCreate() {
        lifecycle.currentState = Lifecycle.State.CREATED
        Timber.i("onCreate")
    }

    fun onStart() {
        lifecycle.currentState = Lifecycle.State.STARTED
        Timber.i("onStart")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(Runnable {
            bindCameraUseCases(cameraProviderFuture.get())
        }, mainExecutor)
    }

    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()


        // The issue will not occur if I remove the imageAnalysis use case.
        val imageAnalysis = ImageAnalysis.Builder()
            .setTargetAspectRatio(AspectRatio.RATIO_16_9)
            .build()

        imageAnalysis.setAnalyzer(
            mainExecutor,
            ImageAnalysis.Analyzer { image: ImageProxy ->

                // Do some analysis...
                val jpgBytes = yuv420888ToJpeg(image)
                image.close()
                onUpdatedListeners.forEach { it(jpgBytes) }
            })

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis)
    }

    private fun yuv420888ToJpeg(image: ImageProxy): ByteArray {
        if (image.format != ImageFormat.YUV_420_888) throw IllegalArgumentException("Wrong image format: ${image.format}")

        val yBuffer = image.planes[0].buffer.apply { rewind() }
        val uBuffer = image.planes[1].buffer.apply { rewind() }
        val vBuffer = image.planes[2].buffer.apply { rewind() }

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val stream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, stream)
        return stream.toByteArray()
    }

    fun onResume() {
        lifecycle.currentState = Lifecycle.State.RESUMED
        Timber.i("onResume")

    }

    fun onPause() {
        lifecycle.currentState = Lifecycle.State.STARTED
        Timber.i("onPause")
    }

    fun onStop() {
        lifecycle.currentState = Lifecycle.State.CREATED
        Timber.i("onStop")
    }

    fun onDestroy() {
        lifecycle.currentState = Lifecycle.State.DESTROYED
        Timber.i("onDestroy")
    }

    override fun getLifecycle(): Lifecycle = lifecycle
}