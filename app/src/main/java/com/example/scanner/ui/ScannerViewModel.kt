package com.example.scanner.ui

import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.ViewModel
import com.example.scanner.usecaseholder.Previewer
import kotlinx.coroutines.flow.StateFlow

enum class ScanMode {
    Barcode,
    Text
}

class DetectedItem(val text:String, val area:Rect, val count:Int)


class ScannerViewModel: ViewModel() {
    private val preview = Previewer()
    val surfaceRequest:StateFlow<SurfaceRequest?> = preview.surfaceRequest
    private var _scanMode:ScanMode = ScanMode.Barcode
    val scanMode: ScanMode get() = _scanMode
    val detectedItems = mutableStateListOf<DetectedItem>()
    val liveDetection = mutableStateListOf<DetectedItem>()
    fun startScanBarcode():Unit {
        _scanMode = ScanMode.Barcode
    }

    fun startScanText():Unit {
        _scanMode = ScanMode.Text

    }
}
