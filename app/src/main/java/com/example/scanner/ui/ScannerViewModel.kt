package com.example.scanner.ui

import android.content.Context
import android.util.Log
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
    private val TAG = "ScannerApp"
    private val cameraScanner = CameraScanner.getInstane()
    private val reportedItems = mutableStateListOf<DetectedItem>()
    val detectedItems = mutableStateListOf<DetectedItem>()
    val liveDetection = mutableStateListOf<DetectedItem>()

    var scanMode = cameraScanner.scanMode
    val surfaceRequest: StateFlow<SurfaceRequest?> = cameraScanner.surfaceRequest

    fun startScan(context: Context, lifecycleOwner: LifecycleOwner) {
        reportedItems.clear()
        viewModelScope.launch {
            cameraScanner.startScan(context, lifecycleOwner)
            cameraScanner.detectResult.collect { detected->
                liveDetection.clear()
                for(detect in detected){
                    liveDetection.add(DetectedItem(detect.text, detect.area, 1))
                    val item = reportedItems.find{ item-> item.text == detect.text}
                    if (item != null){
                        item.count++
                        item.area = detect.area
                        if (item.count > 5) {
                            if (!detectedItems.any{detect-> detect.text == item.text}) {
                                Log.d(TAG, "add to detectedItems: '${item.text}'")
                                detectedItems.add(item)
                                Log.d(TAG, "detectedItems: count=${detectedItems.count()}")
                            }
                        }
                    } else {
                        Log.d(TAG,"add to reportedItems: '${detect.text}'" )
                        reportedItems.add(DetectedItem(detect.text, detect.area, 1))
                        Log.d(TAG, "reportedItems: count(${reportedItems.count()}), detectedItems: count(${detectedItems.count()})")
                    }
                }
            }
        }
    }
    fun toggleScanMode() {
        cameraScanner.toggleScanMode()
    }

    fun resetScan() {
        viewModelScope.launch {
            liveDetection.clear()
            reportedItems.clear()
            detectedItems.clear()
            Log.d(TAG, "resetScan: reportedItems: count(${reportedItems.count()}), detectedItems: count(${detectedItems.count()})")
        }
    }
    fun tapAt(offset: Offset) {
        cameraScanner.tapAt(offset)
    }

    fun setZoom(zoom: Float) {
        cameraScanner.setZoom(zoom)
    }
}
