package com.example.scanner.repository

import android.util.Log

private val TAG = "ScannerApp"

class RegisteredItem(
    var barcode:String,
    var manufacturer:String,
    var series:String? = null,
    var category:String? = null,
    var name:String? = null,
    var count:Int = 1
    )
{
    constructor(barcode: String):this(barcode, getManufacturerCode(barcode), null, null, barcode, 1)

    fun isEmpty(): Boolean {
        return series == null && category == null && name == null
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
                PropertyType.Category -> "Category"
                PropertyType.Name -> "Name"
            }
        }
        fun getManufacturerCode(barcode:String):String {
            val typeCode = barcode.substring(0, 3).toInt()
            if (typeCode in 456..459) {
                return barcode.substring(0, 9)
            }

            return barcode.substring(0, 7)
        }
    }
}

class RegisteredItems {
    private val _items = mutableListOf<RegisteredItem>()

    val knownManufacturers = mutableListOf<ItemProperty>()
    val knownSeries = mutableListOf<ItemProperty>()

    val items:List<RegisteredItem> = _items
    fun setItemProperty(barcode:String, propertyType: RegisteredItem.PropertyType, newValue:String) {
        val item = _items.find{item-> item.barcode == barcode}?:addItem(barcode)
        updateItemProperty(item, propertyType, newValue)
    }
    private fun updateItemProperty(item: RegisteredItem, propertyType: RegisteredItem.PropertyType, newValue:String) {
        Log.d(TAG, "item(${item.barcode}) propertyType:${propertyType}, value:'${newValue}'")
        item.apply {
            if (propertyType == RegisteredItem.PropertyType.Manufacturer) {
                val mpart = RegisteredItem.getManufacturerCode(barcode)
                Log.d(TAG, "mpart:${mpart}")
                val m = knownManufacturers.find { property -> property.barcode == mpart }?.apply {
                    Log.d(TAG, "update knowMAnufactueres: ${mpart}:${newValue}")
                    name = newValue
                }
                if (m == null) {
                    Log.d(TAG, "add to knowManufactueres: ${mpart}:${newValue}")
                    knownManufacturers.add(ItemProperty(0, mpart, newValue))
                }
                manufacturer = newValue
            } else if (propertyType == RegisteredItem.PropertyType.Series) {
                series?.let { code ->
                    if (code.all { it.isDigit() }) {
                        knownSeries.add(ItemProperty(1, code, newValue))
                    }
                }
                series = newValue
            } else if (propertyType == RegisteredItem.PropertyType.Name) {
                name = newValue
            }
        }
    }
    fun getItem(barcode:String): RegisteredItem {
        val item = _items.find{item-> item.barcode == barcode}
        if (item != null) {
            return item
        }

        var manufacturer = RegisteredItem.getManufacturerCode(barcode)
        knownManufacturers.find{ propery-> propery.barcode == manufacturer}?.run {
            manufacturer = name
        }
        val series = knownSeries.find {property -> barcode.startsWith(property.barcode)}

        return RegisteredItem(barcode, manufacturer, series?.name, null, null)
    }
    fun addItem(barcode:String): RegisteredItem {
        var item = _items.find{item-> item.barcode == barcode}
        if (item != null) {
            item.count++
            return item
        }
        item = RegisteredItem(barcode)
        Log.d(TAG, "item.manufacturer:'${item.manufacturer}'")
        var m = knownManufacturers.find{ propery-> propery.barcode == item.manufacturer}?.apply {
            item.manufacturer = name
        }

        if (m == null) {
            knownManufacturers.add(ItemProperty(0, item.manufacturer, item.manufacturer))
        }
        knownSeries.find {property -> barcode.startsWith(property.barcode)}?.let{property ->
            item.series = property.name
        }
        _items.add(item)
        return item
    }
}

data class ItemProperty(
    val uId:Int,
    val barcode:String,
    var name:String
)
