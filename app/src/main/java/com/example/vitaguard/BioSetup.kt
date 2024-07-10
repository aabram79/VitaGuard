package com.example.vitaguard

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.vitaguard.ui.one_time_settings.OneTimeSettingsFragment


class BioSetup : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_bio_setup)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bioSetup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn = findViewById<Button>(R.id.submitButton)


        btn.setOnClickListener{
            val intent = Intent(this, BioSetup2::class.java)
            startActivity(intent)
        }


    }
}