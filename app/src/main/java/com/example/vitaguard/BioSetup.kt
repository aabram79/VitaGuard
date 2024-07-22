package com.example.vitaguard

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.provider.MediaStore.Audio.Radio
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitaguard.ui.one_time_settings.OneTimeSettingsFragment


class BioSetup : AppCompatActivity() {

    val firstTimeSetup = "placeholder"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bio_setup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bioSetup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val btn = findViewById<Button>(R.id.submitButton)
        val txt1 = findViewById<EditText>(R.id.weightField)
        val genderType = findViewById<RadioGroup>(R.id.genderButton)
        val warning = findViewById<TextView>(R.id.warningMessage)
        val feetValues = resources.getStringArray(R.array.heightItems)
        val inchValues = resources.getStringArray(R.array.heightInches)
        val feet = findViewById<Spinner>(R.id.feetField)
        val inches = findViewById<Spinner>(R.id.inchField)
        var selectedFeet = "--"
        var selectedInches = "--"
        val adapter1 = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,feetValues)
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_item)
        feet.adapter = adapter1
        feet.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedFeet = feetValues[position]
                Toast.makeText(this@BioSetup, "Selected item: $selectedFeet", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>){

            }
        }



        val adapter2 = ArrayAdapter(this,
            android.R.layout.simple_spinner_item,inchValues)
        inches.adapter = adapter2
        inches.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedInches = inchValues[position]
                Toast.makeText(this@BioSetup, "Selected item: $selectedInches", Toast.LENGTH_SHORT).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>){

            }
        }





        if(!sharedPref.getBoolean(firstTimeSetup,true)) {
            if(!sharedPref.getBoolean("passwordSetup",false)) {
                val intent = Intent(this, PasswordScreen::class.java)
                startActivity(intent)
            }
            else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

        }

        btn.setOnClickListener{
            if (selectedFeet == "--" || selectedInches == "--"){
                warning.visibility = View.VISIBLE
            }
            else if (txt1.getText().toString() == ""){
                warning.visibility = View.VISIBLE
            }
            else if (genderType.checkedRadioButtonId == -1) {
                warning.visibility = View.VISIBLE
            }
            else {
                val temp = txt1.text.toString()
                val weight = temp.toInt()
                val gender = genderType.checkedRadioButtonId
                editor.apply {
                    putString("feet", selectedFeet)
                    putString("inches", selectedInches)
                    putInt("weight", weight)
                    putInt("gender", gender)
                    putBoolean(firstTimeSetup, false)
                    apply()
                }
                val intent = Intent(this, BioSetup2::class.java)
                startActivity(intent)
            }



        }


    }
}