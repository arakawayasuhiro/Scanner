package com.example.scanner.ui.contents

import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.SurfaceRequest
import androidx.camera.viewfinder.compose.MutableCoordinateTransformer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.setFrom
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.scanner.ui.DetectedItem

@Composable
fun PreviewContents(
    surfaceRequest: SurfaceRequest?,
    detected: List<DetectedItem>,
    onTap: (tapPoint: Offset)-> Unit,
    onLongPress: (pressPoint: Offset)-> Unit,
    onZoom: (zoomRate:Float)->Unit,
    modifier: Modifier = Modifier){
    var poi by remember{mutableStateOf<Offset?>(null)}
    val viewToImageTransformer = remember { MutableCoordinateTransformer() }
    val surfaceTransformationInfo by
    produceState<SurfaceRequest.TransformationInfo?>(null, surfaceRequest) {
        try {
            surfaceRequest?.setTransformationInfoListener(Runnable::run) {transformationInfo ->
                value = transformationInfo
            }
        }
        finally {
            surfaceRequest?.clearTransformationInfoListener()
        }
    }
    Box(modifier) {
        if (surfaceRequest != null) {
            CameraXViewfinder(
                surfaceRequest = surfaceRequest,
                coordinateTransformer = viewToImageTransformer,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(onTap, onLongPress) {
                        detectTapGestures(onLongPress = {offset->
                            onLongPress(offset)
                            poi = null
                        }) { offset ->
                            poi = offset
                            val imageOffset = with(viewToImageTransformer) {
                                offset.transform()
                            }
                            onTap(imageOffset)
                        }
                    }
                    .pointerInput(onZoom) {
                        detectTransformGestures { _, _, zoom, _ ->
                            onZoom(zoom)
                        }
                    }
            )
        } else {
            Box(modifier = modifier) {
                Text(
                    "Camera is not ready", Modifier
                        .padding(4.dp)
                        .padding(top = 24.dp)
                )
            }
        }

        Canvas(
            Modifier
                .fillMaxSize()
                .clip(RectangleShape)
        ) {
            poi?.let {
                val radius = 40f
                drawCircle(
                    color = Color.Green,
                    radius = radius,
                    center = it,
                    alpha = 0.2f,
                    style = Fill
                    )
                drawCircle(
                    color = Color.Green,
                    radius = radius,
                    center = it,
                    style = Stroke(radius / 8f)
                )
            }
            val bufToUi = Matrix().apply {
                setFrom(viewToImageTransformer.transformMatrix)
                invert()
            }

            val sensorToBuf = Matrix().apply {
                surfaceTransformationInfo?.let {
                    setFrom(it.sensorToBufferTransform)
                }
            }

            for (detection in detected){
                detection.area?.let {
                    val uiRect = bufToUi.map(sensorToBuf.map(it))
                    drawRect(
                        color = Color.Red,
                        uiRect.topLeft,
                        uiRect.size,
                        style = Stroke(width =3.0f))
                }
            }
        }
    }
}

