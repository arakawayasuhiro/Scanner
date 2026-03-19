package com.example.scanner.ui.contents

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.DetectedItem

@Composable
fun PreviewContents(
    surfaceRequest: SurfaceRequest?,
    detected: List<DetectedItem>,
    onTap: (tapPoint: Offset)-> Unit,
    onZoom: (zoomRate:Float)->Unit,
    modifier: Modifier = Modifier){
    if (surfaceRequest != null) {
        var coordinateTransformer = remember { MutableCoordinateTransformer() }
        CameraXViewfinder(
            surfaceRequest = surfaceRequest,
            coordinateTransformer = coordinateTransformer,
            modifier = modifier
                .pointerInput(onTap) {
                    detectTapGestures { offset->
                        val sensorOffset = with(coordinateTransformer) {
                            offset.transform()
                        }
                        onTap(offset)
                    }
                }
                .pointerInput(onZoom) {
                    detectTransformGestures { _, _, zoom, _ ->
                        onZoom(zoom)
                    }
                })
    } else {
        Box(modifier = modifier) {
            Text("Camera is not ready", Modifier
                .padding(4.dp)
                .padding(top = 24.dp))
        }
    }
}

