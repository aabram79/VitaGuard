package com.example.vitaguard.ui.settings

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.vitaguard.databinding.FragmentSettingsBinding
import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.setFragmentResult

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var takePermission: ActivityResultLauncher<String>
    private lateinit var takeResultLauncher: ActivityResultLauncher<Intent>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val settingsViewModel =
            ViewModelProvider(this).get(SettingsViewModel::class.java)

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //logic for setting up the bluetooth button
        bluetoothManager = requireActivity().getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        takePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                takeResultLauncher.launch(intent)
            } else {
                Toast.makeText(requireActivity(),"Bluetooth Permission not given", Toast.LENGTH_SHORT).show()
            }
        }
        takeResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback {
                    res->
                if(res.resultCode == ComponentActivity.RESULT_OK){
                    Toast.makeText(requireActivity(),"Bluetooth On", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireActivity(),"Bluetooth denied", Toast.LENGTH_SHORT).show()
                }
            })

        /*
        //How the "This is notifications Fragment" text is displayed
        val textView: TextView = binding.textNotifications
        settingsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        */

        val btnBluetooth: Button = binding.buttonBluetooth
        val btnGoodHealth: Button = binding.buttonGoodHealth
        val btnMidHealth: Button = binding.buttonMidHealth
        val btnBadHealth: Button = binding.buttonBadHealth

        btnBluetooth.setOnClickListener{ enableBluetooth()}
        btnGoodHealth.setOnClickListener{ debugGoodHealth()}
        btnMidHealth.setOnClickListener{ debugMidHealth()}
        btnBadHealth.setOnClickListener{ debugBadHealth()}
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun enableBluetooth(){
        takePermission.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
    }
    private fun debugGoodHealth(){
        val healthData: Bundle = debugHealth(110.0,65.0,83.0,97.3)
        healthData.putString("color","g")
        setFragmentResult("health",healthData)
    }

    private fun debugMidHealth(){
        val healthData: Bundle = debugHealth(124.0,71.0,99.0,94.8)
        healthData.putString("color","y")
        setFragmentResult("health",healthData)
    }

    private fun debugBadHealth(){
        val healthData: Bundle = debugHealth(130.0,55.0,111.5,77.5)
        healthData.putString("color","r")
        setFragmentResult("health",healthData)
    }

    private fun debugHealth(bps: Double, bpd: Double, hr: Double, bo: Double): Bundle {
        val healthData: Bundle = Bundle()
        healthData.putDouble("bps",bps)
        healthData.putDouble("bpd",bpd)
        healthData.putDouble("hr",hr)
        healthData.putDouble("bo",bo)
        return healthData
    }
}