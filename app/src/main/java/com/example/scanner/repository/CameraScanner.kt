package com.example.scanner.repository

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

enum class ScanMode {
    Barcode,
    Text
}

class CameraScanner {
    private var _scanMode:ScanMode = ScanMode.Barcode
    val scanMode: ScanMode get() = _scanMode

    private var meteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    val surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)

    private val preview = Preview.Builder().build().apply {
        setSurfaceProvider {request ->
            surfaceRequest.value = request
            request.resolution.run {
                meteringPointFactory = SurfaceOrientedMeteringPointFactory(
                    width.toFloat(),
                    height.toFloat()
                )
            }
        }
    }

    private val imageScanner = ImageAnalysis.Builder().build()

    var camera: Camera? = null

    suspend fun bindToCamera(context:Context, lifecycleOwner: LifecycleOwner) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        provider.unbindAll()
        camera = provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageScanner
        )
    }
    fun startScanBarcode() {
        _scanMode = ScanMode.Barcode
        imageScanner.setAnalyzer(Executors.newSingleThreadExecutor()) {

        }
    }

    fun startScanText() {
        _scanMode = ScanMode.Text
    }
    suspend fun startScan(context:Context, lifecycleOwner: LifecycleOwner) {
        bindToCamera(context, lifecycleOwner)
        startScanBarcode()
    }

    fun toggleScanMode() {
        if (_scanMode == ScanMode.Barcode) {
            startScanText()
        } else {
            startScanBarcode()
        }
    }
    private fun surfaceOffsetToSensor(offset: Offset) : MeteringPoint? {
        return meteringPointFactory?.createPoint(offset.x, offset.y)
    }

    fun tapAt(offset: Offset) {
        camera?.cameraControl?.run {
            val point = surfaceOffsetToSensor(offset)
            point?.let {
                val meteringAction = FocusMeteringAction.Builder(it).build()
                startFocusAndMetering(meteringAction)
            }
        }

        // TODO: set POI to Scanner
    }

    fun setZoom(zoom:Float) {
        camera?.run {
            cameraInfo.zoomState.value?.let {zoomState->
                cameraControl.setZoomRatio(zoomState.zoomRatio * zoom)
            }
        }
    }
}