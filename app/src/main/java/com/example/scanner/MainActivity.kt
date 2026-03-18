package com.example.scanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.scanner.repository.RegisteredItems
import com.example.scanner.ui.contents.MainContents
import com.example.scanner.ui.contents.PermissionDock
import com.example.scanner.ui.theme.ScannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val requestPermissions = listOf(Manifest.permission.CAMERA)
        val isAllGranted = requestPermissions.all {
            baseContext.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        val registeredItems = RegisteredItems()
        setContent {
            ScannerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    PermissionDock(
                        requestPermissions,
                        isAllGranted,
                        modifier = Modifier.padding(innerPadding),
                        {
                            Text("Not Granted")
                        }
                    ) {
                        MainContents(registeredItems, false, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ScannerTheme {
        Greeting("Android")
    }
}