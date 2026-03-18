package com.example.scanner.repository

class RegisteredItem(
    val barcode:String?,
    val manufacturer:String?,
    val series:String?,
    val category:String?,
    val name:String?
    ) {
    fun isEmpty(): Boolean {
        return barcode == null && manufacturer == null && series == null && category == null && name == null
    }
}

class RegisteredItems {
    private val _items = mutableListOf<RegisteredItem>()
    val items:List<RegisteredItem> = _items

    fun addItem(item: RegisteredItem):Boolean {
        if (item.isEmpty()) {
            return false
        }
        _items.add(item)
        return true
    }
}