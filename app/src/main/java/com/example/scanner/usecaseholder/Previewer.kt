package com.example.scanner.usecaseholder

import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.core.UseCase
import kotlinx.coroutines.flow.MutableStateFlow

class Previewer(): UsecaseHolder {
    val surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)

    override val useCase: UseCase = Preview.Builder().build().apply {
        setSurfaceProvider {request ->
            surfaceRequest.value = request
        }
    }
}