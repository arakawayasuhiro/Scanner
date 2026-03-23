package com.example.scanner.ui.contents

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.scanner.ui.DetectedItem

@Composable
fun BarcodeDetectContents(detected:List<DetectedItem>, modifier: Modifier = Modifier) {
    LazyColumn(modifier)  {
        items(items = detected, key = {item-> item.text}) {item->
              Text("detected: ${item.text}")
        }
    }
}