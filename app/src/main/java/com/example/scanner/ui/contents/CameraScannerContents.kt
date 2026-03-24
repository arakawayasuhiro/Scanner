package com.example.scanner.ui.contents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scanner.repository.RegisteredItem
import com.example.scanner.repository.ScanMode
import com.example.scanner.ui.DetectedItem
import com.example.scanner.ui.ScannerViewModel
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun CameraScannerContents(
    requestPropertyType: RegisteredItem.PropertyType,
    onSelectBarcode:(String) -> Unit,
    onSelectText:(String)-> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScannerViewModel = ScannerViewModel()) {

    val request by viewModel.surfaceRequest.collectAsStateWithLifecycle()
    val previewWeight = 1f
    val listWeight = 0.2f

    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    LaunchedEffect(lifecycleOwner) {
        viewModel.startScan(context, lifecycleOwner, if (requestPropertyType == RegisteredItem.PropertyType.Barcode) ScanMode.Barcode else ScanMode.Text)
    }

    val scanMode = viewModel.scanMode.observeAsState()
    Column(modifier.padding(4.dp)) {
        PreviewContents(
            request,
            viewModel.liveDetection,
            onTap = {offset-> viewModel.tapAt(offset)},
            onZoom = {zoom-> viewModel.setZoom(zoom)},
            modifier = Modifier
                .padding(4.dp)
                .weight(previewWeight)
        )
        if (scanMode.value == ScanMode.Barcode) {
            BarcodeDetectContents(viewModel.detectedItems, onSelect = {text-> onSelectBarcode(text)}, Modifier.weight(listWeight))
        } else {
            TextDetectContents(viewModel.detectedItems, onSelect = {text-> onSelectText(text)},  Modifier.weight(listWeight))
        }
        Row(Modifier.padding(4.dp)) {
            Button(onClick = {viewModel.toggleScanMode()}, Modifier.padding(4.dp)) {
                val text = if (scanMode.value == ScanMode.Barcode) "Scan Text" else "Scan Barcode"
                Text(text)
            }
            Button(onClick = {viewModel.resetScan()}, Modifier.padding(4.dp)) {
                Text("Restart")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 240, heightDp = 480)
@Composable
fun CameraScannerContentsPreview() {
    val viewModel = ScannerViewModel()
    viewModel.detectedItems.add(DetectedItem("AAA", Rect(0f, 0f, 100f, 100f), 1))
    viewModel.detectedItems.add(DetectedItem("BBB", Rect(0f, 0f, 100f, 100f), 1))
    viewModel.detectedItems.add(DetectedItem("CCC", Rect(0f, 0f, 100f, 100f), 1))
    ScannerTheme {
        CameraScannerContents(RegisteredItem.PropertyType.Barcode, {}, {}, viewModel = viewModel)
    }
}
