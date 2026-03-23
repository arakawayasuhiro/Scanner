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

class DetectedItem(val text:String, var area:Rect?, var count:Int)

class ScannerViewModel: ViewModel() {
    private val cameraSanner = CameraScanner()
    private val reportedItems = mutableStateListOf<DetectedItem>()
    val detectedItems = mutableStateListOf<DetectedItem>()
    val liveDetection = mutableStateListOf<DetectedItem>()

    var scanMode = cameraSanner.scanMode
    val surfaceRequest: StateFlow<SurfaceRequest?> = cameraSanner.surfaceRequest

    fun startScan(context: Context, lifecycleOwner: LifecycleOwner) {
        reportedItems.clear()
        viewModelScope.launch {
            cameraSanner.startScan(context, lifecycleOwner)
            cameraSanner.detectResult.collect {detected->
                liveDetection.clear()
                for(detect in detected){
                    liveDetection.add(DetectedItem(detect.text, detect.area, 1))
                    val item = reportedItems.find{ item-> item.text == detect.text}
                    if (item != null){
                        item.count++
                        item.area = detect.area
                        if (item.count > 5) {
                            if (!detectedItems.any{detect-> detect.text == item.text}) {
                                detectedItems.add(item)
                            }
                        }
                    } else {
                        reportedItems.add(DetectedItem(detect.text, detect.area, 1))
                    }
                }
            }
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
