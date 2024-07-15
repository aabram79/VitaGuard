package com.example.vitaguard

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.provider.MediaStore.Audio.Radio
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
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

        if(!sharedPref.getBoolean(firstTimeSetup,true)) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btn.setOnClickListener{
            if (txt1.getText().toString() == ""){
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