package com.example.vitaguard


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.vitaguard.databinding.ActivityMainBinding
import com.example.vitaguard.utils.ConnectThread
import com.example.vitaguard.utils.ConnectedThread
import com.example.vitaguard.utils.HealthDataPoint
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var takePermission: ActivityResultLauncher<String>
    private lateinit var takeResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var connectToBTObservable:Observable<String>
    private lateinit var btReadings: TextView
    private lateinit var adapter: ArrayAdapter<String>


    //We declare a default UUID to create the global variable
    private var arduinoUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var healthReadings = ArrayList<HealthDataPoint>()
    private val ERROR_READ = 0
    private var foundBTDevice = false
    var data = "err"
    var bpm = 0.0
    var sp02 = 0.0

    private lateinit var arduinoBTModule: BluetoothDevice
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Instances of the Android UI elements that will will use during the execution of the APP
        val btn: Button = binding.searchBluetooth
        val btnScan: Button = binding.buttonScan

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, stringList)
        val listView: ListView = findViewById(R.id.display_list)
        listView.adapter = adapter

        btn.setOnClickListener{ searchDevices()}
        btnScan.setOnClickListener{repeatScan(1)}
        //logic for setting up the bluetooth
        bluetoothManager = getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        btReadings = findViewById<TextView>(R.id.btReadings)
        takePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                takeResultLauncher.launch(intent)
            } else {
                Toast.makeText(this,"Bluetooth Permission not given", Toast.LENGTH_SHORT).show()
            }
        }
        takeResultLauncher=registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { res->
                if(res.resultCode == ComponentActivity.RESULT_OK){
                    Toast.makeText(this,"Bluetooth On", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"Bluetooth denied", Toast.LENGTH_SHORT).show()
                }
            }

        //Using a handler to update the interface in case of an error connecting to the BT device
        //My idea is to show handler vs RxAndroid
        var handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                if (msg.what == ERROR_READ) {
                    val arduinoMsg = msg.obj.toString() // Read message from Arduino
                    // replace this with the text output:
                    // btReadings.setText(arduinoMsg)
                }
            }
        }

        // Create an Observable from RxAndroid
        //The code will be executed when an Observer subscribes to the the Observable
        connectToBTObservable = Observable.create { emitter ->
            Log.d("Main Activity", "Calling connectThread class")
            /*Call the constructor of the ConnectThread class
     Passing the Arguments: an Object that represents the BT device,
     the UUID and then the handler to update the UI */
            val connectThread = ConnectThread(arduinoBTModule, arduinoUUID, handler)
            connectThread.run()
            //Check if Socket connected
            if (connectThread.mmSocket!!.isConnected) {
                Log.d("Main Activity", "Calling ConnectedThread class")
                //The pass the Open socket as arguments to call the constructor of ConnectedThread
                val connectedThread: ConnectedThread = ConnectedThread(connectThread.mmSocket!!)
                connectedThread.run()
                if (connectedThread.getValueRead() != null) {
                    // If we have read a value from the Arduino
                    // we call the onNext() function
                    //This value will be observed by the observer
                    emitter.onNext(connectedThread.getValueRead())
                }
                //We just want to stream 1 value, so we close the BT stream
                connectedThread.cancel()
            }
            //Then we close the socket connection
            connectThread.cancel()
            //We could Override the onComplete function
            emitter.onComplete()
        }

        enableBluetooth()
    }

    fun enableBluetooth(){
        takePermission.launch(android.Manifest.permission.BLUETOOTH_CONNECT)
    }

    @SuppressLint("MissingPermission")
    fun searchDevices(){
        var pairedDevices = bluetoothAdapter.bondedDevices
        if (pairedDevices.size > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (device in pairedDevices) {
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                Log.d("Main Activity", "deviceName:$deviceName")
                Log.d("Main Activity", "deviceHardwareAddress:$deviceHardwareAddress")
                //We append all devices to a String that we will display in the UI

                //If we find the HC 05 device (the Arduino BT module)
                //We assign the device value to the Global variable BluetoothDevice
                //We enable the button "Connect to HC 05 device"
                if (deviceName == "Health Monitor") {
                    Log.d("Main Activity", "ESP32 found")
                    arduinoUUID = device.uuids[0].uuid
                    arduinoBTModule = device
                    foundBTDevice = true
                    Toast.makeText(this,"Connected to ESP32_HealthMonitor", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }
    fun getBluetoothAdapter(): BluetoothAdapter {
        return bluetoothAdapter
    }

    fun getUUID(): UUID {
        return arduinoUUID
    }

    fun getArduinoBTModule(): BluetoothDevice {
        return arduinoBTModule
    }

    fun getFoundBTDevice(): Boolean {
        return foundBTDevice
    }

     @SuppressLint("CheckResult")
     fun getReadings(): String {
         btReadings.text = ""
         Toast.makeText(this,"Found Device: $foundBTDevice",Toast.LENGTH_SHORT).show()
        if (foundBTDevice) {
            connectToBTObservable.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io())
                .subscribe { valueRead: String? ->
                    btReadings.text = valueRead
                    //Avg BPM=36, SpO2=96.28, Force=1.00
                    if(valueRead != null){
                        if(valueRead == ""){
                            //TODO: initiate a 5 second notice before calling
                        }

                        data = valueRead.filter { setOf('.','1','2','3','5','6','7','8','9','0',',') .contains(it)}
                    }
                }
            return data
        }
         return "error"
    }

    // Function to update the list using a ListIterator
    private fun updateList() {
        val listIterator = stringList.listIterator()

        // Traverse the list and modify elements
        while (listIterator.hasNext()) {
            val element = listIterator.next()
            if (element == "Cherry") {
                listIterator.set("X") // Replace "Cherry" with "X"
            }
        }

    fun repeatScan(seconds: Long){

        CoroutineScope(Dispatchers.Main).launch {
            while (true){
                data = getReadings()
                if(data != "err"){

                    bpm = data.substringBefore(',').toDouble()
                    data = data.substringAfter(',')
                    sp02 = data.substringAfter(',').toDouble()
                    data = data.substringAfter(',')
                    Log.d("Main Activity","BPM is $bpm, sp02 is $sp02, force is $data")
                }
                delay(seconds * 1000)
            }
        }
    }
}