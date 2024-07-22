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

class PasswordScreen : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.passScreen)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("myPref", MODE_PRIVATE)
        val editor = sharedPref.edit()

        val submitBtn = findViewById<Button>(R.id.mainScreen)
        val bypassBtn = findViewById<Button>(R.id.Bypass)
        val confirmPass = findViewById<EditText>(R.id.passwordEnter)


        submitBtn.setOnClickListener{
            val verifyPass = confirmPass.getText().toString()
            if(sharedPref.getString("password","") == verifyPass) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
        }

        bypassBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}