package com.example.scanner.ui

import android.content.Context
import androidx.camera.core.SurfaceRequest
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanner.repository.CameraScanner
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetectedItem(val text:String, val area:Rect, val count:Int)

class ScannerViewModel: ViewModel() {
    private val cameraSanner = CameraScanner()
    val detectedItems = mutableStateListOf<DetectedItem>()
    val liveDetection = mutableStateListOf<DetectedItem>()

    var scanMode = cameraSanner.scanMode
    val surfaceRequest: StateFlow<SurfaceRequest?> = cameraSanner.surfaceRequest

    fun startScan(context: Context, lifecycleOwner: LifecycleOwner) {
        viewModelScope.launch {
            cameraSanner.startScan(context, lifecycleOwner)
        }
    }
    fun toggleScanMode() {
        cameraSanner.toggleScanMode()
    }

    fun tapAt(offset: Offset) {
        cameraSanner.tapAt(offset)
    }

    fun setZoom(zoom: Float) {
        cameraSanner.setZoom(zoom)
    }
}
