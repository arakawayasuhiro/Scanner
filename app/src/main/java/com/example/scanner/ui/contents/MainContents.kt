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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scanner.repository.RegisteredItem
import com.example.scanner.repository.RegisteredItems
import com.example.scanner.ui.ScannerViewModel
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun MainContents(registeredItems: RegisteredItems, initialIsList:Boolean, modifier: Modifier = Modifier, viewModel: ScannerViewModel = ScannerViewModel()) {
    var isList by remember { mutableStateOf(initialIsList) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (isList) {
            RegisteredItemsContents(
                registeredItems.items,
                onRequest = {item, propertyType->
                    registeredItems.targetItem = item
                    registeredItems.requestPropertyType = propertyType
                    isList = false
                },
                Modifier.weight(1f))
        } else {
            CameraScannerContents(
                registeredItems.requestPropertyType,
                onSelectBarcode = {text->
                    registeredItems.setProperty(RegisteredItem.PropertyType.Barcode, text)
                    isList = true
                },
                onSelectText = {text->
                    registeredItems.setProperty(text)

                    isList = true
                },
                Modifier.weight(1f), viewModel)
        }
        Row(Modifier.align(Alignment.CenterHorizontally)) {
            Button(onClick = { isList= false}, Modifier.padding(4.dp)) {
                Text("Scan")
            }
            Button(onClick = {isList = true}, Modifier.padding(4.dp)) {
                Text("List")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400, heightDp = 800,
    name = "Show List")
@Composable
fun MainContentsListPreview() {
    val items = RegisteredItems()
    items.addItem(RegisteredItem("11111222233331051", "Creos", "Mr.Color", null, "C105"))
    items.addItem(RegisteredItem("11111222233332011", "Creos", "Mr.Color", null, "C201"))
    items.addItem(RegisteredItem("11111222233333071", "Creos", "Mr.Color", null, "C307"))
    ScannerTheme {
        MainContents(items, true)
    }
}@Preview(showBackground = true, widthDp = 400, heightDp = 800,
    name = "Show List")
@Composable
fun MainContentsScanPreview() {
    val items = RegisteredItems()
    items.addItem(RegisteredItem("11111222233331051", "creos", "Mr.Color", null, "C105"))
    items.addItem(RegisteredItem("11111222233332011", "creos", "Mr.Color", null, "C201"))
    items.addItem(RegisteredItem("11111222233333071", "creos", "Mr.Color", null, "C307"))
    ScannerTheme {
        MainContents(items, false)
    }
}