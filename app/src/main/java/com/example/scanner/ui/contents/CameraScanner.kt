package com.example.scanner.ui.contents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scanner.ui.DetectedItem
import com.example.scanner.ui.ScanMode
import com.example.scanner.ui.ScannerViewModel
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun CameraScanner(modifier: Modifier = Modifier, viewModel: ScannerViewModel = ScannerViewModel()) {
    val request by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val previewWeight = 1f
    val listWeight = 0.3f
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.startScan(context, lifecycleOwner)
    }

    val scanMode = remember {viewModel.scanMode}
    Column(modifier.padding(4.dp)) {
        PreviewContents(
            request,
            viewModel.liveDetection,
            onTap = {},
            Modifier
                .padding(4.dp)
                .weight(previewWeight)
        )
        if (scanMode == ScanMode.Barcode) {
            BarcodeDetectContents(viewModel.detectedItems, Modifier.weight(listWeight))
        } else {
            TextDetectContents(viewModel.detectedItems, Modifier.weight(listWeight))
        }
        Row(Modifier.padding(4.dp)) {
            Button(onClick = {viewModel.toggleScanMode(context, lifecycleOwner)}, Modifier.padding(4.dp)) {
                val text = if (scanMode == ScanMode.Barcode) "Scan Text" else "Scan Barcode"
                Text(text)
            }
            Button(onClick = {}, Modifier.padding(4.dp)) {
                Text("List")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 240, heightDp = 480)
@Composable
fun CameraScannerPreview() {
    val viewModel = ScannerViewModel()
    viewModel.detectedItems.add(DetectedItem("AAA", Rect(0f, 0f, 100f, 100f), 1))
    viewModel.detectedItems.add(DetectedItem("BBB", Rect(0f, 0f, 100f, 100f), 1))
    viewModel.detectedItems.add(DetectedItem("CCC", Rect(0f, 0f, 100f, 100f), 1))
    ScannerTheme {
        CameraScanner(viewModel = viewModel)
    }
}
