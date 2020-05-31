package com.example.legoorganizer.activities

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.legoorganizer.R
import kotlinx.android.synthetic.main.activity_options.*

class OptionsActivity : AppCompatActivity() {

    var prefix: String = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    var suffix: String = ".xml"
    var showArchived : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        val extras = intent.extras ?: return
        prefix = extras.getString("Prefix") as String
        suffix = extras.getString("Suffix") as String
        showArchived = extras.getBoolean("Archived")

        editAddressPrefix.setText(prefix)
        checkShowArchived.isChecked = showArchived
    }

    override fun finish() {
        val intent = Intent()
        intent.putExtra("NewPrefix", editAddressPrefix.text.toString())
        intent.putExtra("NewChecked", checkShowArchived.isChecked)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
    }
}
