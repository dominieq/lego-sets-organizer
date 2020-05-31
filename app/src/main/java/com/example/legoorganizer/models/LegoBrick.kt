package com.example.legoorganizer.models

class LegoBrick(
    var id: Int?,
    var inventoryId: Int,
    var typeId: Int,
    var itemId: Int,
    var quantityInSet: Int,
    var quantityInStore: Int,
    var colorId: Int,
    var extra: Int
) {
    var foundEverything: Boolean = false

    fun checkCompleteness(){
        foundEverything = quantityInSet == quantityInStore
    }
}