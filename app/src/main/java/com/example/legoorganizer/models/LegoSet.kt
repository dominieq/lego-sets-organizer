package com.example.legoorganizer.models

import java.io.Serializable

class LegoSet(
    var id: Int,
    var name: String,
    var active: Int,
    var lastAccessed: Int
): Serializable