package com.example.scanner.ui.contents

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.scanner.repository.RegisteredItem
import com.example.scanner.ui.theme.ScannerTheme

@Composable
fun RegisteredItemsContents(registeredItems: List<RegisteredItem>, onRequest:(RegisteredItem?, RegisteredItem.PropertyType)-> Unit, modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        items(items = registeredItems) {item->
            ItemRow(item, {properyType-> onRequest(item, properyType)},  Modifier)
        }
    }
}

@Composable
fun ItemRow(item: RegisteredItem, onRequest:(RegisteredItem.PropertyType)->Unit, modifier: Modifier = Modifier){
    Column(modifier) {
        ItemDetailRow(RegisteredItem.PropertyType.Barcode, item.barcode, {onRequest(RegisteredItem.PropertyType.Barcode)})
        ItemDetailRow(RegisteredItem.PropertyType.Manufacturer, item.manufacturer, {onRequest(RegisteredItem.PropertyType.Manufacturer)})
        ItemDetailRow(RegisteredItem.PropertyType.Series, item.series, {onRequest(RegisteredItem.PropertyType.Series)})
        ItemDetailRow(RegisteredItem.PropertyType.Category, item.category, {onRequest(RegisteredItem.PropertyType.Category)})
        ItemDetailRow(RegisteredItem.PropertyType.Name, item.name, { onRequest(RegisteredItem.PropertyType.Name)})
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

@Preview(showBackground = false, widthDp = 400, heightDp = 800)
@Composable
fun RegisteredItemsContentsPreview() {
    val items = listOf(
        RegisteredItem("1112223331051", "Creos", "Mr.color", "", "C105"),
        RegisteredItem("1112223333011", "Creos", "Mr.color", "", "C301"),
        RegisteredItem("1112223332081", "Creos", "Mr.color", "", "C208"),
        RegisteredItem("8882223332081", "Vallejo", "Model Air", "", "78.0001"),
        RegisteredItem("8882223332081", "Vallejo", "Model Color", "", "79.0001"),
    )
    ScannerTheme {
        RegisteredItemsContents(items, onRequest = {_, _->})
    }
}
