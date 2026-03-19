package com.example.scanner.ui

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.usecaseholder.BarcodeScanner
import com.example.scanner.usecaseholder.Previewer
import com.example.scanner.usecaseholder.TextScanner
import com.example.scanner.usecaseholder.UseCaseHolder
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ScanMode {
    Barcode,
    Text
}

class DetectedItem(val text:String, val area:Rect, val count:Int)

class ScannerViewModel: ViewModel() {
    var camera: Camera? = null
    private val preview = Previewer()
    private val barcodeScanner = BarcodeScanner()
    private val textScanner = TextScanner()
    val surfaceRequest:StateFlow<SurfaceRequest?> = preview.surfaceRequest
    private var _scanMode:ScanMode = ScanMode.Barcode
    val scanMode: ScanMode get() = _scanMode
    val detectedItems = mutableStateListOf<DetectedItem>()
    val liveDetection = mutableStateListOf<DetectedItem>()
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
    suspend fun startScanBarcode(context: Context, lifecycleOwner: LifecycleOwner) {
        _scanMode = ScanMode.Barcode
        bindToCamera(context, lifecycleOwner, barcodeScanner)
    }

    suspend fun startScanText(context: Context, lifecycleOwner: LifecycleOwner) {
        _scanMode = ScanMode.Text
        bindToCamera(context, lifecycleOwner, textScanner)
    }
    fun startScan(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            if (_scanMode == ScanMode.Barcode) {
                startScanBarcode(context, lifecycleOwner)
            } else {
                startScanText(context, lifecycleOwner)
            }
        }
    }
    fun toggleScanMode(context: Context, lifecycleOwner: LifecycleOwner) {
        if (_scanMode == ScanMode.Barcode) {
            viewModelScope.launch {
                startScanText(context, lifecycleOwner)
            }
        } else {
            viewModelScope.launch {
                startScanBarcode(context, lifecycleOwner)
            }
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

    fun setZoom(zoom: Float) {
        camera?.run {
            cameraInfo.zoomState.value?.let {zoomState->
                cameraControl.setZoomRatio(zoomState.zoomRatio * zoom)
            }
        }
    }
}
