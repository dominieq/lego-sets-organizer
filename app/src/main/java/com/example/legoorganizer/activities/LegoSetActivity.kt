package com.example.legoorganizer.activities

import com.example.legoorganizer.models.LegoBrick
import com.example.legoorganizer.database.MyDBHandler
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.legoorganizer.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_lego_set.*

class LegoSetActivity : AppCompatActivity() {

    private lateinit var inventoryBricks: ArrayList<LegoBrick>
    private lateinit var adapter: BrickAdapter
    private var chosenBrick: LegoBrick? = null
    val myDBHandler: MyDBHandler =
        MyDBHandler(this, null, null, 1)
    val urlFullBrick: String = "https://www.lego.com/service/bricks/5/2/"
    val urlOldBrick: String = "http://img.bricklink.com/P/"
    val urlStrangeBrick: String = "https://www.bricklink.com/PL/"


    inner class BrickAdapter(private val context: Context,
                             private val dataSource: ArrayList<LegoBrick>): BaseAdapter() {

        private val inflater: LayoutInflater
            = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getCount(): Int {
            return dataSource.size
        }

        override fun getItem(position: Int): Any {
            return dataSource[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val rowView: View = inflater.inflate(R.layout.lego_brick_item, parent, false)
            val titleTextView: TextView? = rowView.findViewById(R.id.lego_brick_list_title) as TextView?
            val subtitleTextView: TextView? = rowView.findViewById(R.id.lego_brick_list_subtitle) as TextView?
            val detailsTextView: TextView? = rowView.findViewById(R.id.lego_brick_list_detail) as TextView?
            val imageView: ImageView? = rowView.findViewById(R.id.lego_brick_list_thumbnail) as ImageView?

            val legoBrick: LegoBrick = getItem(position) as LegoBrick

            titleTextView?.text = myDBHandler.getPartName(legoBrick.itemId.toString())
            subtitleTextView?.text = myDBHandler.getColorName(legoBrick.colorId.toString())
            val designId: Int? = myDBHandler.getDesignId(legoBrick.itemId, legoBrick.colorId)
            // detailsTextView?.text = designId?.toString()
            if (designId != null) {
                Picasso.with(context).load(urlFullBrick + "$designId")
                    .placeholder(R.mipmap.ic_launcher).into(imageView)
            }

            if(legoBrick.foundEverything) {
                detailsTextView?.text = "Found everything"
                detailsTextView?.setTextColor(android.graphics.Color.RED)
                rowView.setBackgroundColor(android.graphics.Color.GREEN)
            } else {
                detailsTextView?.text = "${legoBrick.quantityInStore}/${legoBrick.quantityInSet}"
                detailsTextView?.setTextColor(android.graphics.Color.BLACK)
                rowView.setBackgroundColor(android.graphics.Color.WHITE)
            }
            return rowView
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lego_set)

        val extras = intent.extras ?: return
        val message = extras.getInt("ChosenInventory")

        this.inventoryBricks = this.myDBHandler.getInventoriesParts(message.toString())

        lego_brick_list_view.setOnItemClickListener{
            parent, view, position, id -> this.chosenBrick = parent.getItemAtPosition(position) as LegoBrick
        }

        this.adapter = BrickAdapter(this, this.inventoryBricks)
        lego_brick_list_view.adapter = this.adapter
    }

    override fun finish() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }

    fun handleIncreaseNumber(view: View) {
        if(chosenBrick != null) {
            if(!chosenBrick!!.foundEverything || chosenBrick!!.quantityInStore < chosenBrick!!.quantityInSet) {
                chosenBrick!!.quantityInStore += 1
                chosenBrick!!.checkCompleteness()
                myDBHandler.updateIncreaseBrickNumber(this.chosenBrick!!)
            }else {
                Toast.makeText(this, "You've already gathered every brick", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Choose brick to increase it's number", Toast.LENGTH_LONG).show()
        }
        lego_brick_list_view.invalidateViews()
    }

    fun handleDecreaseNumber(view: View) {
        if(chosenBrick != null) {
            if(chosenBrick!!.quantityInStore > 0) {
                chosenBrick!!.quantityInStore -= 1
                chosenBrick!!.checkCompleteness()
                myDBHandler.updateDecreaseBrickNumber(chosenBrick!!)
            } else {
                Toast.makeText(this, "You've already had none", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Choose brick to decrease it's number", Toast.LENGTH_LONG).show()
        }
        lego_brick_list_view.invalidateViews()
    }
}
