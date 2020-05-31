package com.example.legoorganizer.activities

import com.example.legoorganizer.models.LegoBrick
import com.example.legoorganizer.models.LegoSet
import com.example.legoorganizer.database.MyDBHandler
import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import com.example.legoorganizer.R
import kotlinx.android.synthetic.main.activity_new_set.*
import org.w3c.dom.Element
import org.xml.sax.InputSource
import java.net.URL
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList

class NewSetActivity : AppCompatActivity() {

    lateinit var databaseManager: MyDBHandler
    lateinit var urlPrefix: String
    lateinit var urlSuffix: String
    private var newId: Int = 1
    private var setNumber: Int = 0
    private var setName: String = ""
    private var setActive: Int = 1
    private var setLastAccessed: Int = Date().time.toInt()

    inner class XMLGetter(private var number: String, private var inventoryId: Int):
        AsyncTask<String, String, String>() {

        override fun doInBackground(vararg params: String): String {
            val urlString = urlPrefix + this.number + urlSuffix
            val url = URL(urlString)

            val dbFactory = DocumentBuilderFactory.newInstance()
            val dBuilder = dbFactory.newDocumentBuilder()
            val xmlInput = dBuilder.parse(InputSource(url.openStream()))
            val brickList = xmlInput.getElementsByTagName("ITEM")
            val toInsert: ArrayList<LegoBrick> = ArrayList()

            for (i in 0 until brickList.length) {
                val legoBrickNode: Element = brickList.item(i) as Element

                val itemTypeCode: String = legoBrickNode.getElementsByTagName("ITEMTYPE")
                    .item(0).textContent;
                val itemIdCode: String = legoBrickNode.getElementsByTagName("ITEMID")
                    .item(0).textContent
                val quantityInSet: String = legoBrickNode.getElementsByTagName("QTY")
                    .item(0).textContent
                val colorCode: String = legoBrickNode.getElementsByTagName("COLOR")
                    .item(0).textContent
                val extra: String = legoBrickNode.getElementsByTagName("EXTRA")
                    .item(0).textContent
                val alternate: String = legoBrickNode.getElementsByTagName("ALTERNATE")
                    .item(0).textContent

                val itemTypeId: Int? = databaseManager.getIDFromTable(itemTypeCode, "ItemTypes")
                val itemId: Int? = databaseManager.getIDFromTable(itemIdCode, "Parts")
                val colorId: Int? = databaseManager.getIDFromTable(colorCode, "Colors")
                val extraValue: Int = if(extra == "Y") 1 else 0

                if(alternate == "N") {
                    if(itemTypeId != null && itemId != null && colorId != null) {
                        val legoBrick =
                            LegoBrick(
                                null, inventoryId, itemTypeId, itemId,
                                quantityInSet.toInt(), 0, colorId, extraValue
                            )
                        toInsert.add(legoBrick)
                    }
                }
            }
            databaseManager.addInventoryParts(toInsert)
            return "XML file processed successfully"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_set)

        databaseManager =
            MyDBHandler(this, null, null, 1)
        val extras = intent.extras?: return
        urlPrefix = extras.getString("Prefix", "http://fcds.cs.put.poznan.pl/MyWeb/BL/")
        urlSuffix = extras.getString("Suffix", ".xml")
    }

    override fun finish() {
        val intent = Intent()
        intent.putExtra("SetId", newId)
        intent.putExtra("SetName", "${setNumber}_$setName")
        intent.putExtra("SetActive", setActive)
        intent.putExtra("SetLastAccessed", setLastAccessed)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    fun acceptNewInventory(view: View) {
        setNumber = legoSetNumber.text.toString().toInt()
        val legoSetName = when(setNumber) {
            70403 -> "Smocza Góra"
            10179 -> "Sokół Millenium UCS"
            361 -> "Kawiarenka"
            10258 -> "Londyński Autobus"
            384 -> "Londyński Autobus"
            555 -> "Szpital"
            else -> "Error"
        }
        if (setName == "Error") {
            Toast.makeText(this, "Nie ma takiego zestawu", Toast.LENGTH_LONG).show()
        } else {
            setActive = 1
            setLastAccessed = Date().time.toInt()
            val legoSet = LegoSet(
                setNumber,
                legoSetName,
                setActive,
                setLastAccessed
            )
            databaseManager.addInventory(legoSet)
            newId = this.databaseManager.getInventoryByLastAccess(setLastAccessed)!!

            if (URLUtil.isValidUrl(urlPrefix + setNumber + urlPrefix)) {
                val xmlGetter = XMLGetter(setNumber.toString(), newId)
                xmlGetter.execute()
            }
            finish()
        }
    }

    fun dismissChanges(view: View) {
        finish()
    }
}
