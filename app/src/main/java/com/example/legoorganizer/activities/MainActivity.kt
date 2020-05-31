package com.example.legoorganizer.activities

import com.example.legoorganizer.database.MyDBHandler
import com.example.legoorganizer.models.LegoSet
import com.example.legoorganizer.models.LegoBrick
import com.example.legoorganizer.options.Options
import android.app.Activity
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.TextView
import android.widget.Toast
import com.example.legoorganizer.R
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var databaseManager: MyDBHandler
    private lateinit var optionsManager: Options
    private lateinit var inventories: ArrayList<LegoSet>
    private var archivedInventories: ArrayList<LegoSet> = ArrayList()
    private lateinit var adapter: InventoryAdapter
    private var chosenInventory: LegoSet? = null
    private val newInventoryNumber = 10000
    private val editInventoryNumber = 20000
    private val optionsNumber = 30000

    inner class InventoryAdapter(
        context: Context, private val dataSource: ArrayList<LegoSet>): BaseAdapter() {

        private val inflater: LayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return this.dataSource.size
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val rowView: View = inflater.inflate(R.layout.lego_set_item, parent, false)
            val titleTextView: TextView? = rowView.findViewById(R.id.lego_set_list_title) as TextView?
            val subtitleTextView: TextView? = rowView.findViewById(R.id.lego_set_list_subtitle) as TextView?
            val detailTextView: TextView? = rowView.findViewById(R.id.lego_set_list_detail) as TextView?

            val legoSet: LegoSet = getItem(position) as LegoSet

            titleTextView?.text = legoSet.name
            subtitleTextView?.text = legoSet.lastAccessed.toString()
            detailTextView?.text = "ID: ${legoSet.id}"
            return rowView
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.databaseManager =
            MyDBHandler(this, null, null, 1)
        this.optionsManager = Options()
        this.inventories = this.databaseManager.getInventories()

        lego_set_list_view.onItemClickListener = OnItemClickListener {
            parent, view, position, id -> this.chosenInventory = parent.getItemAtPosition(position) as LegoSet
        }

        this.adapter = InventoryAdapter(this, this.inventories)
        lego_set_list_view.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if((requestCode == newInventoryNumber) && (resultCode == Activity.RESULT_OK)) {

            if(data != null) {
                var setNumber = 0
                var setName = ""
                var setActive = 1
                var setLastAccessed = Date().time.toInt()

                if (data.hasExtra("SetId")) {
                    setNumber = data.extras!!.getInt("SetId")
                }
                if (data.hasExtra("SetName")) {
                    setName = data.extras!!.getString("SetName") as String
                }
                if (data.hasExtra("SetActive")) {
                    setActive = data.extras!!.getInt("SetActive")
                }
                if (data.hasExtra("SetLastAccessed")) {
                    setLastAccessed = data.extras!!.getInt("SetLastAccessed")
                }

                val legoSet = LegoSet(
                    setNumber,
                    setName,
                    setActive,
                    setLastAccessed
                )
                inventories.add(legoSet)
            }
            adapter.notifyDataSetChanged()

        } else if((requestCode == editInventoryNumber) && (resultCode == Activity.RESULT_OK)) {

        } else if((requestCode == optionsNumber) && (resultCode == Activity.RESULT_OK)) {

            if(data != null) {
                if (data.hasExtra("NewPrefix")) {
                    optionsManager.urlPrefix = data.extras!!.getString("NewPrefix") as String
                }
                if (data.hasExtra("NewChecked")) {
                    optionsManager.showArchived = data.extras!!.getBoolean("NewChecked")
                    if(optionsManager.showArchived) {
                        for (entry: LegoSet in archivedInventories) inventories.add(entry)
                    } else {
                        for (entry: LegoSet in archivedInventories) inventories.remove(entry)
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        }
        lego_set_list_view.invalidateViews()
    }

    fun addNewInventory(view: View) {
        val intent = Intent(this, NewSetActivity::class.java)
        intent.putExtra("Prefix", this.optionsManager.urlPrefix)
        intent.putExtra("Suffix", this.optionsManager.urlSuffix)
        startActivityForResult(intent, newInventoryNumber)
    }

    fun editInventory(view: View) {
        if(chosenInventory != null) {
            val intent = Intent(this, LegoSetActivity::class.java)
            intent.putExtra("ChosenInventory", this.chosenInventory!!.id)
            startActivityForResult(intent, editInventoryNumber)
        } else {
            Toast.makeText(this, "You should choose inventory first", Toast.LENGTH_LONG).show()
        }
    }

    fun archiveInventory(view: View) {
        if(chosenInventory != null) {
            if(!optionsManager.showArchived) {
                inventories.remove(chosenInventory!!)
                archivedInventories.add(chosenInventory!!)
            } else {
                archivedInventories.add(chosenInventory!!)
            }
        }
        lego_set_list_view.invalidateViews()
    }

    fun exportInventory(view: View) {
        if(this.chosenInventory != null) {
            writeXml(this.chosenInventory!!)
        } else {
            Toast.makeText(this, "You should choose inventory first", Toast.LENGTH_LONG).show()
        }
    }

    fun showOptions(view: View) {
        val intent = Intent(this, OptionsActivity::class.java)
        intent.putExtra("Archived", this.optionsManager.showArchived)
        intent.putExtra("Prefix", this.optionsManager.urlPrefix)
        intent.putExtra("Suffix", this.optionsManager.urlSuffix)
        startActivityForResult(intent, optionsNumber)
    }

    private fun writeXml(legoSet: LegoSet) {
        val docBuilder: DocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
        val doc: Document = docBuilder.newDocument()
        val legoBricks: ArrayList<LegoBrick> = this.databaseManager.getInventoriesParts(legoSet.id.toString())
        val rootElement: Element = doc.createElement("INVENTORY")
        for (legoBrick: LegoBrick in legoBricks) {
            val itemElement: Element = doc.createElement("ITEM")

            val itemType: Element = doc.createElement("ITEMTYPE")
            val itemID: Element = doc.createElement("ITEMID")
            val color: Element = doc.createElement("COLOR")
            val qty: Element = doc.createElement("QTYFILLED")

            val itemTypeCode: String? = this.databaseManager.getCodeFromTable(legoBrick.typeId, "ItemTypes")
            val itemIDCode: String? = this.databaseManager.getCodeFromTable(legoBrick.itemId, "Parts")
            val colorCode: String? = this.databaseManager.getCodeFromTable(legoBrick.colorId, "Colors")
            println("$itemTypeCode $itemIDCode $colorCode")

            itemType.appendChild(doc.createTextNode(itemTypeCode))
            itemID.appendChild(doc.createTextNode(itemIDCode))
            color.appendChild(doc.createTextNode(colorCode))
            qty.appendChild(doc.createTextNode((legoBrick.quantityInSet - legoBrick.quantityInStore).toString()))

            itemElement.appendChild(itemType)
            itemElement.appendChild(itemID)
            itemElement.appendChild(color)
            itemElement.appendChild(qty)

            rootElement.appendChild(itemElement)
        }
        doc.appendChild(rootElement)

        val transformer: Transformer = TransformerFactory.newInstance().newTransformer()
        transformer.setOutputProperty(OutputKeys.INDENT, "yes")
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

        val part = legoSet.name.split("_")[0]
        val filename = "${part}_toGather.xml"
        val path: File? = this.getExternalFilesDir(null)

        val xmlDirectory = File(path, "Output")
        xmlDirectory.mkdirs()
        val file = File(xmlDirectory, filename)

        transformer.transform(DOMSource(doc), StreamResult(file))
    }
}
