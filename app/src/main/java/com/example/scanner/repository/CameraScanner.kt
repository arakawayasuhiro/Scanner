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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.setFrom
import androidx.lifecycle.MutableLiveData
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

enum class ScanMode {
    Barcode,
    Text
}

class DetectResult(val text:String, val area: Rect?)

class CameraScanner {
    val scanMode = MutableLiveData<ScanMode>()
    val detectResult = MutableStateFlow(listOf<DetectResult>())
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

    fun Matrix.map(rect:android.graphics.Rect?):Rect? {
        return rect?.run {
            map(
                Rect(
                    left = left.toFloat(),
                    top = top.toFloat(),
                    right = right.toFloat(),
                    bottom = bottom.toFloat()
                )
            )
        }
    }
    fun startScanBarcode() {
        scanMode.value = ScanMode.Barcode
        imageScanner.setAnalyzer(Executors.newSingleThreadExecutor()) {imageProxy ->
            val options = BarcodeScannerOptions.Builder()
                .enableAllPotentialBarcodes()
                .build()
            val imageToSensorMatrix = Matrix().apply {
                setFrom(imageProxy.imageInfo.sensorToBufferTransformMatrix)
                invert()
            }
            val inputImage =
                InputImage.fromBitmap(imageProxy.toBitmap(), 0)
            val scanner = BarcodeScanning.getClient(options)
            val result = scanner.process(inputImage)
            result.run {
                addOnSuccessListener { barcodes ->
                    val detected = mutableListOf<DetectResult>()
                    for (barcode in barcodes) {
                        barcode.displayValue?.let { code ->
                            if (!code.isEmpty()) {
                                detected.add(DetectResult(
                                    text = code,
                                    area = imageToSensorMatrix.map(barcode.boundingBox)
                                ))
                            }
                        }
                    }
                    detectResult.value = detected
                }
                addOnCompleteListener {
                    imageProxy.close()
                }
            }
        }
    }

    fun startScanText() {
        scanMode.value = ScanMode.Text
        imageScanner.setAnalyzer(Executors.newSingleThreadExecutor()) {imageProxy ->
            val imageToSensorMatrix = Matrix().apply {
                setFrom(imageProxy.imageInfo.sensorToBufferTransformMatrix)
                invert()
            }
            val inputImage =
                InputImage.fromBitmap(imageProxy.toBitmap(), 0)
            val recognizer = TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            val result = recognizer.process(inputImage)
            result
                .addOnSuccessListener {text ->
                    val detected = mutableListOf<DetectResult>()
                    for(block in text.textBlocks) {
                        detected.add(DetectResult(
                            text = block.text,
                            area = imageToSensorMatrix.map(block.boundingBox)
                        ))
                    }
                    detectResult.value = detected
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
    suspend fun startScan(context:Context, lifecycleOwner: LifecycleOwner) {
        bindToCamera(context, lifecycleOwner)
        startScanBarcode()
    }

    fun toggleScanMode() {
        if (scanMode.value == ScanMode.Barcode) {
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