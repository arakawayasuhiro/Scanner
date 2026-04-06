package com.example.scanner.ui.contents

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.scanner.repository.ItemProperty
import com.example.scanner.repository.RegisteredItem
import com.example.scanner.repository.RegisteredItems
import com.example.scanner.ui.ScannerViewModel
import kotlinx.coroutines.flow.MutableStateFlow

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
    var requestPropertyValue by remember{mutableStateOf<String?>(null)}

    var targetItem by remember {mutableStateOf<RegisteredItem?>(null)}
    val items by registeredItems.items.collectAsStateWithLifecycle(listOf<RegisteredItem>(), LocalLifecycleOwner.current)
    val newItemFlow = remember {MutableStateFlow<String?>(null)}
    val newItemRequest by newItemFlow.collectAsState(null)
    var itemToAdd by remember {mutableStateOf<String?>(null)}

    var deleteItemRequest by remember{mutableStateOf<RegisteredItem?>(null)}
    var itemToDelete by remember{mutableStateOf<String?>(null)}

    newItemRequest?.let{request->
        if (!items.any{item-> item.barcode == newItemRequest}){
            LaunchedEffect(request) {
                registeredItems.addItem(request)
                newItemFlow.value = null
                contentsMode = ContentsMode.List
            }
        } else {
            NewItemDialog(newItemRequest!!,
                {
                    itemToAdd = request
                    newItemFlow.value = null
                    contentsMode = ContentsMode.List
                },
                {
                    newItemFlow.value = null
                    contentsMode = ContentsMode.List
                })
        }
    }
    LaunchedEffect(itemToAdd) {
        itemToAdd?.let { request ->
            registeredItems.addItem(request)
        }
    }
    LaunchedEffect(requestPropertyValue, requestPropertyType) {
        Log.d(TAG, "LaunchEffect $requestPropertyValue, $requestPropertyType")
        if (requestPropertyValue != null) {
            targetItem?.run {
                registeredItems.setItemProperty(barcode, requestPropertyType, requestPropertyValue!!)
            }

            requestPropertyValue = null
        }
    }

    deleteItemRequest?.let{item->
        AlertDialog(
            onDismissRequest = {deleteItemRequest = null},
            confirmButton = {
                Button({
                    deleteItemRequest = null
                    itemToDelete = item.barcode
                }){
                    Text("Delete")
                }
            },
            dismissButton = {
                Button({
                    deleteItemRequest = null
                }){
                    Text("Dismiss")
                }
            },
            icon = {Icon(Icons.Default.Warning, "")},
            title = {Text("Delete Item")},
            text = {Text("Delete '${deleteItemRequest?.name?:deleteItemRequest?.barcode}'?")}
        )
    }
    LaunchedEffect(itemToDelete) {
        Log.d(TAG, "delete: '$itemToDelete'")
        itemToDelete?.let {barcode->
            registeredItems.deleteItemByBarcode(barcode)
            itemToDelete = null
        }
    }
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
                    onSetProperty = { item, propertyType, newValue ->
                        Log.d(TAG, "onSetProperty(${item.barcode}, $propertyType, $newValue")
                        targetItem = item
                        requestPropertyType = propertyType
                        requestPropertyValue = newValue
                    },
                    onRequestSelection = { item, propertyType ->
                        targetItem = item
                        requestPropertyType = propertyType
                        contentsMode = ContentsMode.Select
                    },
                    onDepete = {item->
                        deleteItemRequest = item
                    },
                    Modifier.weight(1f)
                )
            }
            ContentsMode.Scan-> {
                CameraScannerContents(
                    requestPropertyType,
                    onSelectBarcode = {text->
                        Log.d(TAG, "onSelectBarcode:'$text'")
                        newItemFlow.value = text
                    },
                    onSelectText = {text->
                        requestPropertyValue = text
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
                            Log.d(TAG, "onSetProperty(${barcode}, $propertyType, $newValue")
                            requestPropertyType = propertyType
                            requestPropertyValue = newValue
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

@Composable
fun NewItemDialog(
    newItem:String,
    onAddStockCount:(String)->Unit,
    onClose:()->Unit
    ) {

    Dialog({}){
        Card(Modifier.fillMaxWidth(), shape =  RoundedCornerShape(16.dp)) {
            Column(Modifier.padding(4.dp)) {
                Text("Increment stock count?", Modifier.padding(4.dp).align(Alignment.CenterHorizontally))
                Row(Modifier.padding(4.dp)) {
                    Button({onAddStockCount(newItem)}, Modifier.padding(4.dp)) {
                        Text("Add Stock")
                    }
                    Button({onClose()}, Modifier.padding(4.dp)) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}