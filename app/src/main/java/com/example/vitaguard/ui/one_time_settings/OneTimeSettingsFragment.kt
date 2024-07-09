package com.example.vitaguard.ui.one_time_settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.vitaguard.MainActivity
import com.example.vitaguard.R
import com.example.vitaguard.databinding.FragmentOneTimeSettingsBinding
import com.example.vitaguard.ui.settings.SettingsViewModel

/*
    TODO: Currently an activity and not a fragment; either refactor or redesign
 */
class OneTimeSettingsFragment : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.fragment_one_time_settings)

        val enablePassSwitch = findViewById<Switch>(R.id.passwordSwitch)
        val passwordText = findViewById<EditText>(R.id.passwordField)
        val confirmButton = findViewById<Button>(R.id.confirmButton)

        confirmButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        enablePassSwitch.setOnCheckedChangeListener{_, isChecked ->
            if(isChecked) passwordText.visibility = View.VISIBLE
            if(!isChecked) passwordText.visibility = View.INVISIBLE
        }
    }
}