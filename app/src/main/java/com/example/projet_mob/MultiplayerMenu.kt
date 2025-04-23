package com.example.projet_mob

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.projet_mob.bluetooth.MyBluetoothService

class MultiplayerMenu : ComponentActivity() {
    private lateinit var myBluetoothService: MyBluetoothService
    private var isBluetoothEnabled = false

    private lateinit var btnEnableBluetooth: Button
    private lateinit var btnStartDiscovery: Button
    private lateinit var btnStartAdvertising: Button
    private lateinit var deviceListView: ListView
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private val deviceNames = mutableListOf<String>()


    private val requestEnableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                isBluetoothEnabled = myBluetoothService.bluetoothAdapter.isEnabled
                Toast.makeText(this, "Bluetooth activé", Toast.LENGTH_SHORT).show()
                updateUIVisibility()
            } else {
                Toast.makeText(this, "Activation de Bluetooth échouée", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer_menu)

        val timerTextView = findViewById<TextView>(R.id.timerTextView)

        myBluetoothService = MyBluetoothService(this, timerTextView)
        myBluetoothService.checkPermissions()

        isBluetoothEnabled = myBluetoothService.bluetoothAdapter.isEnabled

        // 🔗 Récupérer les boutons du layout
        btnEnableBluetooth = findViewById(R.id.btnEnableBluetooth)
        btnStartDiscovery = findViewById(R.id.btnStartDiscovery)
        btnStartAdvertising = findViewById(R.id.btnStartAdvertising)

        deviceListView = findViewById(R.id.bluetoothDeviceList)
        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
        deviceListView.adapter = deviceAdapter

        btnEnableBluetooth.setOnClickListener {
            enableBluetooth()
        }

        btnStartDiscovery.setOnClickListener {
            myBluetoothService.startDiscovery()
        }

        btnStartAdvertising.setOnClickListener {
            myBluetoothService.startAdvertising()
        }

        updateUIVisibility()
        myBluetoothService.setOnDeviceFoundListener { deviceName ->
            runOnUiThread {
                onDeviceFound(deviceName)
            }
        }

    }

    private fun updateUIVisibility() {
        if (myBluetoothService.bluetoothAdapter.isEnabled) {
            btnEnableBluetooth.visibility = Button.GONE
            btnStartDiscovery.visibility = Button.VISIBLE
            btnStartAdvertising.visibility = Button.VISIBLE
        } else {
            btnEnableBluetooth.visibility = Button.VISIBLE
            btnStartDiscovery.visibility = Button.GONE
            btnStartAdvertising.visibility = Button.GONE
        }
    }

    private fun enableBluetooth() {
        if (!myBluetoothService.bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestEnableBluetoothLauncher.launch(enableBtIntent)
        } else {
            Toast.makeText(this, "Bluetooth déjà activé", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myBluetoothService.unregisterReceiver()
    }
    fun onDeviceFound(deviceName: String) {
        if (!deviceNames.contains(deviceName)) {
            deviceNames.add(deviceName)
            deviceAdapter.notifyDataSetChanged()
        }
    }

}
