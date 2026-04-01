package com.example.scanner.ui.contents

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scanner.repository.RegisteredItem
import com.example.scanner.repository.RegisteredItems
import com.example.scanner.ui.ScannerViewModel

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

    Column(modifier = modifier.fillMaxWidth()) {
        when(contentsMode) {
            ContentsMode.List-> {
                RegisteredItemsContents(
                    registeredItems.items,
                    onRequestScan = { item, propertyType ->
                        targetItem = item
                        requestPropertyType = propertyType
                        contentsMode = ContentsMode.Scan
                    },
                    onSetProperty = { item, properyType, newValue ->
                        registeredItems.setItemProperty(item.barcode, properyType, newValue)
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
                        registeredItems.addItem(text)
                        contentsMode = ContentsMode.List
                    },
                    onSelectText = {text->
                        targetItem?.run {
                            registeredItems.setItemProperty(barcode, requestPropertyType, text)
                        }
                        contentsMode = ContentsMode.List
                    },
                    Modifier.weight(1f), viewModel)
            }

            ContentsMode.Select-> {
                val propertyItemList = registeredItems.getItemProperties(requestPropertyType)
                SelectPropertyValueContents(
                    propertyType = requestPropertyType,
                    propertyItems = propertyItemList,
                    onSelected = {propertyType, newValue->
                        targetItem?.run {
                            registeredItems.setItemProperty(barcode, propertyType, newValue)
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
