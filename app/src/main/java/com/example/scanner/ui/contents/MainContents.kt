package com.example.scanner.ui.contents

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.scanner.repository.ItemProperty
import com.example.scanner.repository.RegisteredItem
import com.example.scanner.repository.RegisteredItems
import com.example.scanner.ui.ScannerViewModel
import kotlinx.coroutines.launch

private const val TAG = "ScannerApp"
enum class ContentsMode {
    Scan,
    List,
    Select
}
@Composable
fun MainContents(registeredItems: RegisteredItems, initialMode: ContentsMode, modifier: Modifier = Modifier, viewModel: ScannerViewModel = ScannerViewModel()) {
    var contentsMode by remember { mutableStateOf(initialMode) }
    var requestPropertyType by remember {mutableStateOf(RegisteredItem.PropertyType.Barcode)}
    var targetItem by remember {mutableStateOf<RegisteredItem?>(null)}
    val lifecycleOwner = LocalLifecycleOwner.current
    val items by registeredItems.items.collectAsStateWithLifecycle(listOf<RegisteredItem>(), LocalLifecycleOwner.current)
    Column(modifier = modifier.fillMaxWidth()) {
        when(contentsMode) {
            ContentsMode.List-> {
                RegisteredItemsContents(
                    items,
                    onRequestScan = { item, propertyType ->
                        targetItem = item
                        requestPropertyType = propertyType
                        contentsMode = ContentsMode.Scan
                    },
                    onSetProperty = { item, properyType, newValue ->
                        Log.d(TAG, "onSetProperty(${item.barcode}, $properyType, $newValue")
                        lifecycleOwner.lifecycleScope.launch {
                            registeredItems.setItemProperty(item.barcode, properyType, newValue)
                        }
                    },
                    onRequestSelection = { item, propertyType ->
                        targetItem = item
                        requestPropertyType = propertyType
                        contentsMode = ContentsMode.Select
                    },
                    Modifier.weight(1f)
                )
            }
            ContentsMode.Scan-> {
                CameraScannerContents(
                    requestPropertyType,
                    onSelectBarcode = {text->
                        Log.d(TAG, "onSelectBarcode:'$text'")
                        lifecycleOwner.lifecycleScope.launch {
                            registeredItems.addItem(text)
                            contentsMode = ContentsMode.List
                        }
                    },
                    onSelectText = {text->
                        targetItem?.run {
                            lifecycleOwner.lifecycleScope.launch {
                                registeredItems.setItemProperty(barcode, requestPropertyType, text)
                            }
                        }
                        contentsMode = ContentsMode.List
                    },
                    Modifier.weight(1f), viewModel)
            }

            ContentsMode.Select-> {
                var propertyItemList by remember { mutableStateOf(listOf<ItemProperty>()) }
                LaunchedEffect(propertyItemList) {
                    propertyItemList = registeredItems.getItemProperties(requestPropertyType)
                }

                SelectPropertyValueContents(
                    propertyType = requestPropertyType,
                    propertyItems = propertyItemList,
                    onSelected = {propertyType, newValue->
                        targetItem?.run {
                            lifecycleOwner.lifecycleScope.launch {
                                registeredItems.setItemProperty(barcode, propertyType, newValue)
                            }
                        }
                        contentsMode = ContentsMode.List
                    },
                    onCancel = { contentsMode = ContentsMode.List}
                )
            }
        }
        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick =
                    {
                        contentsMode = ContentsMode.Scan
                        requestPropertyType = RegisteredItem.PropertyType.Barcode
                        targetItem = null
                    }, Modifier.padding(4.dp)) {
                Text("New Item")
            }
            Button(onClick = {contentsMode = ContentsMode.List}, Modifier.padding(4.dp)) {
                Text("List")
            }
        }
    }
}
