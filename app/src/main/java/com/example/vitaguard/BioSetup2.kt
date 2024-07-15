package com.example.vitaguard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class BioSetup2 : AppCompatActivity() {

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_one_time_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val check1 = findViewById<Switch>(R.id.passwordSwitch)
        val password = findViewById<EditText>(R.id.passwordField)
        val btn = findViewById<Button>(R.id.confirmButton)

        check1.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) password.visibility = View.VISIBLE
            if (!isChecked) password.visibility = View.INVISIBLE
        }

        btn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)


        }
    }
}