package com.example.scanner.repository

import android.content.Context
import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

private val TAG = "ScannerApp"

class RegisteredItem(
    @ColumnInfo(name = "barcode") var barcode:String,
    @ColumnInfo(name = "manufacturer") var manufacturer:String,
    @ColumnInfo(name = "category") var category:String? = null,
    @ColumnInfo(name = "name") var name:String? = null,
    @ColumnInfo(name = "count") var count:Int = 1
    )
{
    enum class PropertyType{
        Barcode,
        Manufacturer,
        Category,
        Name,
        Count,
    }

    companion object {
        fun getPropertyLabel(propertyType: PropertyType):String {
            return when (propertyType) {
                PropertyType.Barcode -> "Barcode"
                PropertyType.Manufacturer -> "Manufacturer"
                PropertyType.Category -> "Category"
                PropertyType.Name -> "Name"
                PropertyType.Count-> "Count"
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

class RegisteredItems(context: Context) {
    private val db = Room.databaseBuilder(context, ScannedItemDatabase::class.java, "scanneditem.db").build()
    private val dao = db.scannedItemDao()
    val items get() = dao.getRegisteredItems()

    suspend fun setItemProperty(barcode:String, propertyType: RegisteredItem.PropertyType, newValue:String) {
        dao.getRegisteredItem(barcode)?.let { item ->
            Log.d(TAG, "setItemProperty: item:'${item.barcode}'")
            updateItemProperty(item, propertyType, newValue)
        }
    }

    suspend private fun updateItemProperty(item: RegisteredItem, propertyType: RegisteredItem.PropertyType, newValue:String) {
        item.apply {
            if (propertyType == RegisteredItem.PropertyType.Manufacturer) {
                val mpart = RegisteredItem.getManufacturerCode(barcode)
                dao.updateManufacturer(Manufacturer(mpart, newValue))
                manufacturer = newValue
            } else if (propertyType == RegisteredItem.PropertyType.Category) {
                category?.let { code ->
                    if (code.all { it.isDigit() }) {
                        dao.updateCategory(Category(code, newValue))
                    }
                }
                category = newValue
            } else if (propertyType == RegisteredItem.PropertyType.Name) {
                name = newValue
                dao.updateItem(this)
            }
        }
    }
    suspend fun getItem(barcode:String): RegisteredItem? {
        return dao.getRegisteredItem(barcode)
    }

    suspend fun addItem(barcode:String): RegisteredItem {
        Log.d(TAG, "addItem($barcode)")
        val item = dao.getRegisteredItem(barcode)
        if (item != null) {
            Log.d(TAG, "item($barcode) is found")
            item.count++
            dao.updateItem(item)
            return item
        }

        return dao.registerItem(barcode)
    }

    suspend fun getManufactureres(): List<ItemProperty> {
        return dao.getManufacturers().map {manufacturer ->
            ItemProperty(manufacturer.barcode, manufacturer.name)
        }
    }
    suspend fun getCategories(): List<ItemProperty> {
        return dao.getCategories().map {category ->
            ItemProperty(category.barcode, category.name)
        }
    }
    suspend fun getItemProperties(propertyType: RegisteredItem.PropertyType) : List<ItemProperty>{
        return when (propertyType) {
            RegisteredItem.PropertyType.Manufacturer-> getManufactureres()
            RegisteredItem.PropertyType.Category->getCategories()
            else-> listOf()
        }
    }
}

data class ItemProperty(
    val barcode:String,
    var name:String
)

@Entity(primaryKeys = ["barcode"])
data class ScannedItem(
    @ColumnInfo("barcode") val barcode:String,
    @ColumnInfo("manufacturerCode") val manufacturerCode:String,
    @ColumnInfo("categoryCode") val categoryCode:String?,
    @ColumnInfo("name") var name: String?,
    @ColumnInfo(name = "count") var count:Int,
)

@Entity(primaryKeys = ["barcode"])
data class Manufacturer(
    @ColumnInfo(name = "barcode") val barcode:String,
    @ColumnInfo(name = "name") var name:String,
)
@Entity(primaryKeys = ["barcode"])
data class Category(
    @ColumnInfo(name = "barcode") val barcode:String,
    @ColumnInfo(name = "name") var name:String,
)


@Dao
interface ScannedItemsDao{
    @Query("select * from ScannedItem")
    fun getScannedItems(): List<ScannedItem>

    @Query("select * from ScannedItem where barcode = :barcode")
    suspend fun getScannedItem(barcode:String): ScannedItem
    @Query("select i.barcode as barcode, m.name as manufacturer, c.name as category, i.name as name, i.count as count " +
            "from ScannedItem i inner join Manufacturer m on i.manufacturerCode = m.barcode left outer join Category c on i.categoryCode = c.barcode"
    )
    fun getRegisteredItems(): Flow<List<RegisteredItem>>

    @Query("select i.barcode as barcode, m.name as manufacturer, c.name as category, i.name as name, i.count as count " +
            "from ScannedItem i inner join Manufacturer m on i.manufacturerCode = m.barcode left outer join Category c on i.categoryCode = c.barcode " +
            "where i.barcode = :barcode"
    )
    suspend fun getRegisteredItem(barcode:String): RegisteredItem?

    @Query("select * from Manufacturer where barcode = :mbar")
    suspend fun getManufacturer(mbar:String): Manufacturer?

    @Query("select * from Category where barcode = :cbar")
    suspend fun getCategory(cbar:String): Category

    @Insert
    suspend fun addItem(item: ScannedItem)

    suspend fun registerItem(barcode:String): RegisteredItem {
        Log.d(TAG, "registerItem($barcode)")
        val mbar = RegisteredItem.getManufacturerCode(barcode)
        var m = getManufacturer(mbar)
        if (m == null) {
            m = Manufacturer(barcode = mbar, name = mbar)
            Log.d(TAG, "new ManufacturerCode:'$mbar'")
            addManufacturer(m)
        }

        val newItem = ScannedItem(barcode = barcode, manufacturerCode = m.barcode, categoryCode = null, name = null, count = 1)
        addItem(newItem)
        Log.d(TAG, "regusterItem: addItem completed")
        return RegisteredItem(newItem.barcode, m.name)
    }

    suspend fun updateItem(newItem: RegisteredItem) {
        val item = getScannedItem(newItem.barcode)
        item.count = newItem.count
        item.name = newItem.name
        Log.d(TAG,"updateItem: barcode:${item.barcode} count:${item.count} name:${item.name}")
        updateScannedItem(item)
    }
    @Update
    suspend fun updateScannedItem(item: ScannedItem)

    @Query("select * from Manufacturer")
    suspend fun getManufacturers(): List<Manufacturer>

    @Query("select * from Category")
    suspend fun getCategories(): List<Category>

    @Insert
    suspend fun addManufacturer(manufacturer: Manufacturer)

    @Insert
    suspend fun addCategory(category: Category)

    @Update
    suspend fun updateManufacturer(manufacturer: Manufacturer)

    @Update
    suspend fun updateCategory(category: Category)
}

@Database(entities = [ScannedItem::class, Manufacturer::class, Category::class], version = 1, exportSchema = false)
abstract  class ScannedItemDatabase : RoomDatabase() {
    abstract fun scannedItemDao(): ScannedItemsDao
}