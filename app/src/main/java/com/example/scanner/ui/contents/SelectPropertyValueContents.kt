package com.example.scanner.ui.contents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scanner.repository.ItemProperty
import com.example.scanner.repository.RegisteredItem

@Composable
fun SelectPropertyValueContents(
    propertyType: RegisteredItem.PropertyType,
    propertyItems:List<ItemProperty>,
    onSelected:(RegisteredItem.PropertyType, String)->Unit,
    onCancel:()->Unit,
    modifier: Modifier = Modifier) {
    Column(modifier.padding(4.dp)) {
        var selectedItem by remember {mutableStateOf<ItemProperty?>(null)}
        LazyColumn(Modifier.weight(1f)) {
            items(propertyItems) {item->
                var modifier =Modifier
                    .clickable() {
                        selectedItem = item
                    }
                if (selectedItem == item) {
                    modifier = modifier.background(Color(192, 192, 255))
                }
                Text(item.name, modifier)
            }
        }
        Row(Modifier.padding(4.dp)) {
            Button(
                onClick = {
                    selectedItem?.run {
                        onSelected(propertyType, name)
                    }
                },
                enabled = selectedItem != null)
            {
                Text("Select")
            }
            Button(onClick = {onCancel()}) {
                Text("Cancel")
            }
        }
    }
}