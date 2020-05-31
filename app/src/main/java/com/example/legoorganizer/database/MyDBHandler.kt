package com.example.legoorganizer.database

import com.example.legoorganizer.models.LegoBrick
import com.example.legoorganizer.models.LegoSet
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import java.io.Serializable
import java.util.ArrayList

class MyDBHandler(context: Context, name: String?,
                  factory: SQLiteDatabase.CursorFactory?, version: Int): SQLiteOpenHelper(context,
    DATABASE_NAME,
    factory,
    DATABASE_VERSION
), Serializable{

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "BrickList.db"
        const val TABLE_INVENTORIES = "Inventories"
        const val TABLE_INVENTORIES_PARTS = "InventoriesParts"
        const val COLUMN_INV_ID = "id"
        const val COLUMN_INV_NAME = "Name"
        const val COLUMN_INV_ACTIVE = "Active"
        const val COLUMN_INV_LAST_ACCESSED = "LastAccessed"
        const val COLUMN_INV_PART_ID = "id"
        const val COLUMN_INV_PART_INV_ID = "InventoryID"
        const val COLUMN_INV_PART_TYPE_ID = "TypeID"
        const val COLUMN_INV_PART_ITEM_ID = "ItemID"
        const val COLUMN_INV_PART_Q_INSET = "QuantityInSet"
        const val COLUMN_INV_PART_Q_INSTORE = "QuantityInStore"
        const val COLUMN_INV_PART_COLOR_ID = "ColorID"
        const val COLUMN_INV_PART_EXTRA = "Extra"
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun getIDFromTable(code: String, table: String): Int? {
        val query = "SELECT Id FROM $table WHERE Code = \"$code\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var id: Int? = null

        if (cursor.moveToFirst()) {
            id = Integer.parseInt(cursor.getString(0))
        }

        cursor.close()
        db.close()

        return id
    }

    fun getCodeFromTable(id: Int, table: String): String? {
        val query = "SELECT Code FROM $table WHERE Id = \"$id\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var code: String? = null

        if (cursor.moveToFirst()) {
            code = cursor.getString(0)
        }

        cursor.close()
        db.close()

        return code
    }

    fun getInventories(): ArrayList<LegoSet> {
        val query = "SELECT * FROM $TABLE_INVENTORIES"
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        val legoSetsArrayList: ArrayList<LegoSet> = ArrayList()

        if(cursor.moveToFirst()) {
            do {
                val id: Int = Integer.parseInt(cursor.getString(0))
                val name: String = cursor.getString(1)
                val active: Int = Integer.parseInt(cursor.getString(2))
                val lastAccessed: Int = Integer.parseInt(cursor.getString(3))
                val legoSet = LegoSet(
                    id,
                    name,
                    active,
                    lastAccessed
                )
                legoSetsArrayList.add(legoSet)

            }while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return legoSetsArrayList
    }

    fun getInventoriesParts(inventoryID: String): ArrayList<LegoBrick> {
        val query = "SELECT * FROM $TABLE_INVENTORIES_PARTS WHERE InventoryID = \"$inventoryID\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        val legoBricksArrayList: ArrayList<LegoBrick> = ArrayList()

        if(cursor.moveToFirst()) {
            do {
                val id: Int = Integer.parseInt(cursor.getString(0))
                val inventoryId: Int = Integer.parseInt(cursor.getString(1))
                val typeId: Int = Integer.parseInt(cursor.getString(2))
                val itemId: Int = Integer.parseInt(cursor.getString(3))
                val quantityInSet: Int = Integer.parseInt(cursor.getString(4))
                val quantityInStore: Int = Integer.parseInt(cursor.getString(5))
                val colorId: Int = Integer.parseInt(cursor.getString(6))
                val extra: Int = Integer.parseInt(cursor.getString(7))
                val legoBrick = LegoBrick(
                    id, inventoryId, typeId, itemId,
                    quantityInSet, quantityInStore, colorId, extra
                )
                legoBrick.checkCompleteness()
                legoBricksArrayList.add(legoBrick)

            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return legoBricksArrayList
    }

    fun getPartName(itemId: String): String? {
        val query = "SELECT Name FROM Parts WHERE Id=\"$itemId\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var name: String? = null

        if(cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()
        db.close()

        return name
    }

    fun getColorName(colorId: String): String? {
        val query = "SELECT Name FROM Colors WHERE Id=\"$colorId\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var name: String? = null

        if(cursor.moveToFirst()) {
            name = cursor.getString(0)
        }

        cursor.close()
        db.close()

        return name
    }


    fun getDesignId(itemId: Int, colorId: Int): Int? {
        val query = "SELECT Code FROM Codes WHERE ItemID = \"$itemId\" AND ColorID = \"$colorId\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var code: Int? = null

        if(cursor.moveToFirst()) {
            code = Integer.parseInt(cursor.getString(0))
        }

        cursor.close()
        db.close()

        return code
    }

    fun getInventoryByLastAccess(lastAccessed: Int): Int? {
        val query = "SELECT Id FROM Inventories WHERE LastAccessed = \"$lastAccessed\""
        val db: SQLiteDatabase = this.writableDatabase
        val cursor: Cursor = db.rawQuery(query, null)
        var id: Int? = null

        if (cursor.moveToFirst()) {
            id = Integer.parseInt(cursor.getString(0))
        }

        cursor.close()
        db.close()

        return id
    }

    fun updateIncreaseBrickNumber(brick: LegoBrick) {
        val values = ContentValues()
        values.put(COLUMN_INV_PART_Q_INSTORE, brick.quantityInStore)

        val db: SQLiteDatabase = this.writableDatabase
        db.update(TABLE_INVENTORIES_PARTS, values, "id = " + brick.id, null)
        db.close()
    }

    fun updateDecreaseBrickNumber(brick: LegoBrick) {
        val values = ContentValues()
        values.put(COLUMN_INV_PART_Q_INSTORE, brick.quantityInStore)

        val db: SQLiteDatabase = this.writableDatabase
        db.update(TABLE_INVENTORIES_PARTS, values, "id = " + brick.id, null)
        db.close()
    }

    fun addInventory(legoSet: LegoSet) {
        val values = ContentValues()

        val name = "${legoSet.id}_${legoSet.name}"
        values.put(COLUMN_INV_NAME, name)
        values.put(COLUMN_INV_ACTIVE, legoSet.active)
        values.put(COLUMN_INV_LAST_ACCESSED, legoSet.lastAccessed)

        val db: SQLiteDatabase = this.writableDatabase
        db.insert(TABLE_INVENTORIES, null, values)
        db.close()
    }

    fun addInventoryParts(legoBricks: ArrayList<LegoBrick>) {
        val db: SQLiteDatabase = this.writableDatabase
        val values = ContentValues()
        for (legoBrick in legoBricks) {
            values.put(COLUMN_INV_PART_INV_ID, legoBrick.inventoryId)
            values.put(COLUMN_INV_PART_TYPE_ID, legoBrick.typeId)
            values.put(COLUMN_INV_PART_ITEM_ID, legoBrick.itemId)
            values.put(COLUMN_INV_PART_Q_INSET, legoBrick.quantityInSet)
            values.put(COLUMN_INV_PART_Q_INSTORE, legoBrick.quantityInStore)
            values.put(COLUMN_INV_PART_COLOR_ID, legoBrick.colorId)
            values.put(COLUMN_INV_PART_EXTRA, legoBrick.extra)
            db.insert(TABLE_INVENTORIES_PARTS, null, values)
        }
        db.close()
    }
}