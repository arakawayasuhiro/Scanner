package com.example.scanner.repository

import androidx.lifecycle.LifecycleOwner
import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.ui.geometry.Offset
import com.example.scanner.usecaseholder.BarcodeScanner
import com.example.scanner.usecaseholder.Previewer
import com.example.scanner.usecaseholder.TextScanner
import com.example.scanner.usecaseholder.UseCaseHolder
import kotlinx.coroutines.flow.StateFlow

enum class ScanMode {
    Barcode,
    Text
}

class CameraScanner {
    private var _scanMode:ScanMode = ScanMode.Barcode
        val scanMode: ScanMode get() = _scanMode

    private val preview = Previewer()
    private val barcodeScanner = BarcodeScanner()
    private val textScanner = TextScanner()

    var camera: Camera? = null
    val surfaceRequest: StateFlow<SurfaceRequest?> = preview.surfaceRequest

    private suspend fun bindToCamera(context:Context, lifecycleOwner: LifecycleOwner, useCaseHolder: UseCaseHolder) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        provider.unbindAll()
        camera = provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview.useCase,
            useCaseHolder.useCase
        )
    }
    private suspend fun startScanBarcode(context: Context, lifecycleOwner: LifecycleOwner) {
        _scanMode = ScanMode.Barcode
        bindToCamera(context, lifecycleOwner, barcodeScanner)
    }

    private suspend fun startScanText(context: Context, lifecycleOwner: LifecycleOwner) {
        _scanMode = ScanMode.Text
        bindToCamera(context, lifecycleOwner, textScanner)
    }
    suspend fun startScan(context:Context, lifecycleOwner: LifecycleOwner) {
        if (_scanMode == ScanMode.Barcode) {
            startScanBarcode(context, lifecycleOwner)
        } else {
            startScanText(context, lifecycleOwner)
        }
    }

    suspend fun toggleScanMode(context:Context, lifecycleOwner: LifecycleOwner) {
        if (_scanMode == ScanMode.Barcode) {
            startScanText(context, lifecycleOwner)
        } else {
            startScanBarcode(context, lifecycleOwner)
        }
    }

    fun tapAt(offset: Offset) {
        camera?.cameraControl?.run {
            val point = preview.surfaceOffsetToSensor(offset)
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