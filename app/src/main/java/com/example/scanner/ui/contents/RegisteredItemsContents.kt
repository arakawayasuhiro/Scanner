package com.example.scanner.ui.contents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.scanner.repository.RegisteredItem

@Composable
fun RegisteredItemsContents(
    registeredItems: List<RegisteredItem>,
    onRequestScan:(RegisteredItem?, RegisteredItem.PropertyType)-> Unit,
    onSetProperty:(RegisteredItem, RegisteredItem.PropertyType, String)->Unit,
    onRequestSelection:(RegisteredItem, RegisteredItem.PropertyType)->Unit,
    onDelete:(RegisteredItem)->Unit,
    modifier: Modifier = Modifier) {
    var requestProperty by remember {mutableStateOf(RegisteredItem.PropertyType.Barcode)}
    var requestItem by remember {mutableStateOf<RegisteredItem?>(null)}

    var deleteItem by remember{mutableStateOf<RegisteredItem?>(null)}
    if (requestItem != null) {
        SelectActionDialog(
            onClose = { requestItem = null },
            onRequestScan =
                {
                    onRequestScan(requestItem, requestProperty)
                    requestItem = null
                },
            onSelectText =
                {newValue->
                    onSetProperty(requestItem!!, requestProperty, newValue)
                    requestItem = null
                },
            onRequestSelection =
                {
                    onRequestSelection(requestItem!!, requestProperty)
                    requestItem = null
                },
        )
    }
    deleteItem?.let{ item->
        AlertDialog(
            onDismissRequest = {deleteItem = null},
            confirmButton = {
                Button({
                    onDelete(item)
                    deleteItem = null
                }){
                    Text("Delete")
                }
            },
            dismissButton = {
                Button({ deleteItem = null } ){
                    Text("Dismiss")
                }
            },
            icon = {Icon(Icons.Default.Warning, "")},
            title = {Text("Delete Item")},
            text = {Text("Delete '${deleteItem?.name?:deleteItem?.barcode}'?")}
        )
    }

    LazyColumn(modifier) {
        items(items = registeredItems, key = {item-> item.barcode}) { item->
            ItemRow(
                item,
                { properyType->
                    requestProperty = properyType
                    requestItem = item
                },
                {
                    deleteItem = item
                },
                Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun ItemRow(
    item: RegisteredItem,
    onRequest:(RegisteredItem.PropertyType)->Unit,
    onDelete:()->Unit,
    modifier: Modifier = Modifier){
    Card(Modifier.padding(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier) {
            Row(Modifier.fillMaxWidth().padding(4.dp)) {
                FilledIconButton({ onDelete()}) {
                    Icon(
                      Icons.Default.Delete,
                        ""
                    )
                }
            }
            ItemDetailRow(RegisteredItem.PropertyType.Barcode, item.barcode, {onRequest(RegisteredItem.PropertyType.Barcode)})
            ItemDetailRow(RegisteredItem.PropertyType.Manufacturer, item.manufacturer, {onRequest(RegisteredItem.PropertyType.Manufacturer)})
            ItemDetailRow(RegisteredItem.PropertyType.Category, item.category, {onRequest(RegisteredItem.PropertyType.Category)})
            ItemDetailRow(RegisteredItem.PropertyType.Name, item.name, { onRequest(RegisteredItem.PropertyType.Name)})
            ItemDetailRow(RegisteredItem.PropertyType.Count, item.count.toString(), {})
        }
    }
}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemDetailRow(propertyType: RegisteredItem.PropertyType, value:String?, onRequest:()->Unit, modifier: Modifier = Modifier) {
    Row(modifier
        .combinedClickable(
            onLongClick = {onRequest()}
        ){}) {
        Text(RegisteredItem.getPropertyLabel(propertyType), Modifier.weight(1f))
        Text(":", modifier.padding(horizontal = 8.dp))
        Text(value?:"---", Modifier.weight(1.5f))
    }
}

@Composable
fun SelectActionDialog(
    onClose:()->Unit,
    onRequestScan:()->Unit,
    onSelectText:(String)->Unit,
    onRequestSelection:()->Unit
    ) {
    Dialog(onDismissRequest = { onClose() }){
        Card(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            var valueText by remember {mutableStateOf("")}
            Column(Modifier.padding(horizontal = 8.dp)) {
                Button({ onRequestScan() }, Modifier.align(alignment = Alignment.End)) {
                    Text("Scan")
                }
                HorizontalDivider(Modifier.padding(8.dp), thickness = 2.dp)
                TextField(valueText, onValueChange = {newText-> valueText = newText}, Modifier.padding(horizontal = 4.dp).fillMaxWidth())
                Button({ onSelectText(valueText) }, Modifier.align(alignment = Alignment.End)) {
                    Text("Assign Text")
                }
                HorizontalDivider(Modifier.padding(8.dp), thickness = 2.dp)
                Button({ onRequestSelection() }, Modifier.align(alignment = Alignment.End)) {
                    Text("Select from list")
                }
            }
        }
    }
}