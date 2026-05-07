package com.example.scanner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.example.scanner.repository.RegisteredItems
import com.example.scanner.ui.contents.ContentsMode
import com.example.scanner.ui.contents.MainContents
import com.example.scanner.ui.contents.PermissionDock
import com.example.scanner.ui.theme.ScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val requestPermissions = listOf(Manifest.permission.CAMERA)
        val registeredItems = RegisteredItems(baseContext)
        setContent {
            ScannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionDock(
                        requestPermissions,
                        modifier = Modifier.padding(innerPadding),
                        {
                            Text("Not Granted")
                        }
                    ) {
                         MainContents(registeredItems, ContentsMode.Scan, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
