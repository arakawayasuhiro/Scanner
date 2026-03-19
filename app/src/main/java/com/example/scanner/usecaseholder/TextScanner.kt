package com.example.scanner.usecaseholder

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.UseCase
import java.util.concurrent.Executors

class TextScanner: UseCaseHolder {
    override val useCase: UseCase = ImageAnalysis.Builder().build().apply {
        setAnalyzer(Executors.newSingleThreadExecutor()){
            // TODO: Text scanning
        }
    }
}