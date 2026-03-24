package com.example.scanner.repository

class RegisteredItem(
    var barcode:String? = null,
    var manufacturer:String? = null,
    var series:String? = null,
    var category:String? = null,
    var name:String? = null
    ) {
    fun isEmpty(): Boolean {
        return barcode == null && manufacturer == null && series == null && category == null && name == null
    }

    fun setProperty(propertyType: PropertyType, value:String) {
        when(propertyType) {
            PropertyType.Barcode-> barcode = value
            PropertyType.Manufacturer-> manufacturer = value
            PropertyType.Series -> series = value
            PropertyType.Category -> category = value
            PropertyType.Name -> name = value
        }
    }
    enum class PropertyType{
        Barcode,
        Manufacturer,
        Series,
        Category,
        Name
    }

    companion object {
        fun getPropertyLabel(propertyType: PropertyType):String {
            return when (propertyType) {
                PropertyType.Barcode -> "Barcode"
                PropertyType.Manufacturer -> "Manufacturer"
                PropertyType.Series -> "Series"
                PropertyType.Category -> "Catetory"
                PropertyType.Name -> "Name"
            }
        }
    }
}

class RegisteredItems {
    private val _items = mutableListOf<RegisteredItem>()
    val items:List<RegisteredItem> = _items
    var targetItem: RegisteredItem? = null
    var requestPropertyType = RegisteredItem.PropertyType.Barcode
    fun addItem(item: RegisteredItem):Boolean {
        if (item.isEmpty()) {
            return false
        }
        _items.add(item)
        return true
    }

    fun setProperty(propertyType: RegisteredItem.PropertyType, value:String) {
        if (targetItem != null){
            targetItem?.setProperty(propertyType, value)
        } else {
            _items.add(RegisteredItem().apply { setProperty(propertyType, value) })
        }

    }
    fun setProperty(value:String) {
        setProperty(requestPropertyType, value)
    }

    fun startNewItem(){
        targetItem = null
        requestPropertyType = RegisteredItem.PropertyType.Barcode
    }
}