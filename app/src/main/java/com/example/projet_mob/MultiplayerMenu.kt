package com.example.projet_mob

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.projet_mob.bluetooth.MyBluetoothService
import com.example.projet_mob.activity.*
import com.example.projet_mob.bluetooth.BluetoothManager

class MultiplayerMenu : ComponentActivity() {
    private lateinit var myBluetoothService: MyBluetoothService
    private var isBluetoothEnabled = false

    private lateinit var btnEnableBluetooth: Button
    private lateinit var btnStartDiscovery: Button
    private lateinit var btnStartAdvertising: Button
    private lateinit var deviceListView: ListView
    private lateinit var deviceAdapter: ArrayAdapter<String>
    private val deviceNames = mutableListOf<String>()
    private val deviceAddresses = mutableListOf<String>()
    private val activityMap = listOf(
        First::class.java,
        Second::class.java,
        Third::class.java,
        Fourth::class.java,
        Fifth::class.java,
        Sixth::class.java
    )


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
        BluetoothManager.bluetoothService = myBluetoothService
        myBluetoothService.checkPermissions()

        isBluetoothEnabled = myBluetoothService.bluetoothAdapter.isEnabled

        // 🔗 Récupérer les boutons du layout
        btnEnableBluetooth = findViewById(R.id.btnEnableBluetooth)
        btnStartDiscovery = findViewById(R.id.btnStartDiscovery)
        btnStartAdvertising = findViewById(R.id.btnStartAdvertising)

        deviceListView = findViewById(R.id.bluetoothDeviceList)
        deviceAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, deviceNames)
        deviceListView.adapter = deviceAdapter

        val btnStartGame = findViewById<Button>(R.id.btnStartGame)
        btnStartGame.visibility = View.GONE

// ⬇️ Définir le callback de connexion Bluetooth
        myBluetoothService.onConnectedCallback = {
            runOnUiThread {
                btnStartGame.visibility = View.VISIBLE // 👈 Affiche le bouton une fois connecté
            }
        }

        btnStartGame.setOnClickListener {
            val randomIds = listOf(0, 1, 2, 3, 4, 5).shuffled().take(3)
            val startGameMessage = "START_GAME:${randomIds.joinToString(",")}"

            MultiplayerGameState.reset()
            myBluetoothService.sendMessage(startGameMessage)

            val intent = Intent(this, MultiplayerGameActivity::class.java).apply {
                putExtra("challenge_ids", randomIds.toIntArray())
            }
            startActivity(intent)
        }

        btnEnableBluetooth.setOnClickListener {
            enableBluetooth()
        }

        btnStartDiscovery.setOnClickListener {
            myBluetoothService.startDiscovery()
        }

        btnStartAdvertising.setOnClickListener {
            myBluetoothService.startListeningForConnections()
            myBluetoothService.startAdvertising()
        }

        updateUIVisibility()
        myBluetoothService.setOnDeviceFoundListener { deviceName, deviceAddress ->
            runOnUiThread {
                onDeviceFound(deviceName, deviceAddress)
            }
        }

        deviceListView.setOnItemClickListener { _, _, position, _ ->
            val deviceAddress = deviceAddresses[position]
            val device = myBluetoothService.bluetoothAdapter.getRemoteDevice(deviceAddress)
            Toast.makeText(this, "Connexion à ${device.name ?: device.address}...", Toast.LENGTH_SHORT).show()
            myBluetoothService.connectToDevice(device) // Tente de se connecter à l'appareil
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

    // Ajouter un appareil à la liste
    fun onDeviceFound(deviceName: String, deviceAddress: String) {
        if (!deviceNames.contains(deviceName)) {
            deviceNames.add(deviceName)
            deviceAddresses.add(deviceAddress) // Ajoutez l'adresse aussi
            deviceAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myBluetoothService.unregisterReceiver()
    }
}
