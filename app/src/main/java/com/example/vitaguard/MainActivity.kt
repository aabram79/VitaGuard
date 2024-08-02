package com.example.vitaguard


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.vitaguard.databinding.ActivityMainBinding
import com.example.vitaguard.ui.settings.SettingsFragment
import com.example.vitaguard.utils.BAD_HEALTH
import com.example.vitaguard.utils.ConnectThread
import com.example.vitaguard.utils.ConnectedThread
import com.example.vitaguard.utils.GOOD_HEALTH
import com.example.vitaguard.utils.HealthDataPoint
import com.example.vitaguard.utils.MID_HEALTH
import com.google.android.material.bottomnavigation.BottomNavigationView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit


private const val ERROR_READ = 0
private const val SCAN_LOOP_TIME = 1 //in seconds
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var arduinoBTModule: BluetoothDevice
    private lateinit var takePermission: ActivityResultLauncher<String>
    private lateinit var takeResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var connectToBTObservable:Observable<String>
    private lateinit var btReadings: TextView
    private lateinit var callDialog: AlertDialog
    private lateinit var pNum: String
    private lateinit var pUri: Uri
    private lateinit var callIntent: Intent

    //We declare a default UUID to create the global variable
    private var arduinoUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var healthReadings = ArrayList<HealthDataPoint>()
    private var foundBTDevice = false
    private var scanJob = true
    private var scanCount = 0
    private var data = "err"
    private var bpm = 0.0
    private var sp02 = 0.0
    private var avgBPM = 0.0
    private var avgSP02 = 0.0
    private var report = ""
    private var currentTime: String = java.util.Date().toString()

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

        //logic for calling permissions

        val callPermReq = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                isGranted ->
            if(isGranted){
                pNum = "9194522487"
                pUri = Uri.parse("tel:$pNum")
                callIntent = Intent(Intent.ACTION_CALL, pUri)
                startActivity(callIntent)
            } else {
                Toast.makeText(this,"Calling permissions denied", Toast.LENGTH_SHORT).show()
            }
        }

        //Instances of the Android UI elements that will will use during the execution of the APP
        val btn: Button = binding.searchBluetooth
        val btnScan: Button = binding.buttonScan
        btReadings = findViewById(R.id.btReadings)
        callDialog = AlertDialog.Builder(this)
            .setTitle("Collision Detected!")
            .setMessage("Call Emergency Contact?")
            .setPositiveButton("Call") { _, _ ->
                callPermReq.launch(Manifest.permission.CALL_PHONE)
            }
            .setNegativeButton("Cancel") { _, _ ->
                scanJob = true
                repeatScan(SCAN_LOOP_TIME)
            }.create()
        btn.setOnClickListener{ searchDevices()}
        btnScan.setOnClickListener{repeatScan(SCAN_LOOP_TIME)}


        callDialog.setOnShowListener { dialog ->
            val defaultButton =
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            val buttonText = defaultButton.text
            object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    defaultButton.text = java.lang.String.format(
                        Locale.getDefault(), "%s (%d)",
                        buttonText,
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1 //add one so it never displays zero
                    )
                }

                override fun onFinish() {
                    if (dialog.isShowing) {
                        dialog.dismiss()
                        //TODO: Add calling function
                    }
                }
            }.start()
        }
        //logic for setting up the bluetooth
        bluetoothManager = getSystemService(ComponentActivity.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

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
                    Log.e("Main Activity",msg.obj.toString()) // Read message from Arduino
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
                        if(valueRead.contains("trauma")){
                            scanJob = false
                            if(!callDialog.isShowing) callDialog.show()
                        } else if (valueRead.contains("Avg")) {
                            data = valueRead.filter { setOf('.','1','2','3','5','6','7','8','9','0',',') .contains(it)}
                        } else {
                            data = "err"
                        }

                    }
                }
        }
         return data
    }

    fun repeatScan(seconds: Long){
        CoroutineScope(Dispatchers.Main).launch {
            while (scanJob){
                data = getReadings()
                if(data != "err"){
                    bpm += data.substringBefore(',').toDouble()
                    data = data.substringAfter(',')
                    sp02 += data.substringAfter(',').toDouble()
                    data = data.substringAfter(',')
                    Log.d("Main Activity","BPM is $bpm, sp02 is $sp02, force is $data")
                    if(scanCount>=10){ //averages out the last 10 readings
                        bpm /= 10
                        sp02 /= 10
                        currentTime = java.util.Date().toString()
                        when{
                            bpm <= 60 || bpm >= 120 || sp02 < 60 || sp02 >= 120 -> healthReadings.add(HealthDataPoint(bpm,sp02, BAD_HEALTH, currentTime))
                            bpm <= 70 || bpm >= 100 || sp02 < 75 || sp02 > 100 -> healthReadings.add(HealthDataPoint(bpm,sp02, MID_HEALTH, currentTime))
                            else -> healthReadings.add(HealthDataPoint(bpm,sp02, GOOD_HEALTH, currentTime))
                        }
                        healthReadings.add(HealthDataPoint(bpm,sp02, GOOD_HEALTH, currentTime))
                        bpm = 0.0
                        sp02 = 0.0
                    }
                }
                if(scanCount >=10) {
                    scanCount = 0;
                }
                else scanCount++
                delay(seconds * 1000)
            }
        }
    }

    fun repeatScan(seconds: Int){
        repeatScan(seconds.toLong())
    }


    fun createReport(){
        report = ""
        if(healthReadings.size > 0){
            avgBPM = 0.0
            avgSP02 = 0.0
            //pauses the scan
            scanJob = false

            for (reading in healthReadings){
                avgBPM += reading.bpm
                avgSP02 += reading.sp02
            }
            avgBPM /= healthReadings.size
            avgSP02 /= healthReadings.size
        } else {
            avgBPM = -1.0
            avgSP02 = -1.0
        }
        report = "Average BPM: $avgBPM, Average sp02: $avgSP02\n\t"
        for (reading in healthReadings.filter { r -> r.code != GOOD_HEALTH }){
            report += "WARNING: ${reading.code} health concern detected at ${reading.date}. BPM: ${reading.bpm}, sp02: $sp02\n\t"
        }
    }

}