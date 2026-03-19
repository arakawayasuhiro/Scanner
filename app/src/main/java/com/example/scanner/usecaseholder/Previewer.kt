package com.example.scanner.usecaseholder

import androidx.camera.core.MeteringPoint
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.flow.MutableStateFlow

class Previewer: UseCaseHolder {
    private var meteringPointFactory: SurfaceOrientedMeteringPointFactory? = null
    val surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)

    override val useCase: UseCase = Preview.Builder().build().apply {
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

    fun surfaceOffsetToSensor(offset: Offset) : MeteringPoint? {
        return meteringPointFactory?.createPoint(offset.x, offset.y)
    }
}