package com.example.legoorganizer.options

import java.io.Serializable

class Options: Serializable {

    var urlPrefix : String = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    var urlSuffix : String = ".xml"
    var showArchived : Boolean = false

}