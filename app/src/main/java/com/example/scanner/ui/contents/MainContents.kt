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

@Composable
fun MainContents(registeredItems: RegisteredItems, initialIsList:Boolean, modifier: Modifier = Modifier, viewModel: ScannerViewModel = ScannerViewModel()) {
    var isList by remember { mutableStateOf(initialIsList) }
    var requestPropertyType by remember {mutableStateOf<RegisteredItem.PropertyType>(RegisteredItem.PropertyType.Barcode)}
    var targetItem by remember {mutableStateOf<RegisteredItem?>(null)}
    Column(modifier = modifier.fillMaxWidth()) {
        if (isList) {
            RegisteredItemsContents(
                registeredItems.items,
                onRequest = {item, propertyType->
                    targetItem = item
                    requestPropertyType = propertyType
                    isList = false
                },
                Modifier.weight(1f))
        } else {
            CameraScannerContents(
                requestPropertyType,
                onSelectBarcode = {text->
                    registeredItems.addItem(RegisteredItem(barcode = text))
                    isList = true
                },
                onSelectText = {text->
                    targetItem?.setProperty(requestPropertyType, text)

                    isList = true
                },
                Modifier.weight(1f), viewModel)
        }
        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick =
                    {
                        isList= false
                        requestPropertyType = RegisteredItem.PropertyType.Barcode
                        targetItem = null
                    }, Modifier.padding(4.dp)) {
                Text("New Item")
            }
            Button(onClick = {isList = true}, Modifier.padding(4.dp)) {
                Text("List")
            }
        }
    }
}
