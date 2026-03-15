package com.example.scanner.ui.contents

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PermissionDock(
    permissions:List<String>,
    hasInitialPermissions: Boolean,
    modifier: Modifier = Modifier,
    contentsNotGranted: @Composable ()-> Unit,
    contentsGranted: @Composable () -> Unit,
    ) {

    var granted by remember {mutableStateOf(hasInitialPermissions) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {results->
        granted = permissions.all{permission->
             results.containsKey(permission) && results[permission]?:false
        }
    }
    if (granted) {
        Box(modifier) {
            contentsGranted()
        }
    } else {
        Column(modifier) {
            Box(Modifier.weight(1f)) {
                contentsNotGranted()
            }
            Button(onClick = {launcher.launch(permissions.toTypedArray())}, Modifier.padding(4.dp)) {
                Text("Request Permissions")
            }
        }
    }
}
