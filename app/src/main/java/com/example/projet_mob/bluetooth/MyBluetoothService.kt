package com.example.projet_mob.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import com.example.projet_mob.MultiplayerGameActivity
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class MyBluetoothService(
    val context: Activity,
    private val timerTextView: TextView
) {
    val bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    private var deviceFoundCallback: ((String) -> Unit)? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseTimer: CountDownTimer? = null

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!discoveredDevices.contains(it)) {
                        discoveredDevices.add(it)
                        val nameOrAddress = it.name?.let { name -> "$name - ${it.address}" } ?: it.address
                        deviceFoundCallback?.invoke(nameOrAddress)
                    }
                }
            }
        }
    }

    private var connectedThread: ConnectedThread? = null
    private var connectionState = mutableStateOf(BluetoothState.DISCONNECTED)
    private var connectionCallback: ((BluetoothState) -> Unit)? = null

    enum class BluetoothState {
        DISCONNECTED, CONNECTING, CONNECTED, ERROR
    }

    private val requestBluetoothPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        (context as? androidx.activity.ComponentActivity)?.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val allGranted = permissions.all { it.value }
            Toast.makeText(
                context,
                if (allGranted) "Permissions Bluetooth accordées" else "Permissions Bluetooth refusées",
                Toast.LENGTH_SHORT
            ).show()
        } ?: throw IllegalStateException("Activity must be a ComponentActivity for permission handling")

    init {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = btManager.adapter
        registerReceiver()
    }

    fun checkPermissions() {
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                add(Manifest.permission.BLUETOOTH_ADVERTISE)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needed.isNotEmpty()) {
            Log.d("Bluetooth", "Permissions manquantes : ${needed.joinToString()}")
            requestBluetoothPermissionsLauncher.launch(needed.toTypedArray())
        } else {
            Log.d("Bluetooth", "Toutes les permissions Bluetooth sont déjà accordées.")
        }
    }

    fun startDiscovery() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
                discoveredDevices.clear()
                bluetoothAdapter.startDiscovery()
            } else {
                Toast.makeText(context, "Permission de scan manquante", Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission lors de la découverte", e)
        }
    }

    fun setOnDeviceFoundListener(callback: (String) -> Unit) {
        this.deviceFoundCallback = callback
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permission Bluetooth manquante", Toast.LENGTH_SHORT).show()
            checkPermissions()
            return
        }

        connectionState.value = BluetoothState.CONNECTING
        connectionCallback?.invoke(connectionState.value)

        Thread {
            try {
                val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                bluetoothAdapter.cancelDiscovery()
                socket.connect()

                connectedThread = ConnectedThread(socket).apply { start() }
                connectionState.value = BluetoothState.CONNECTED
                connectionCallback?.invoke(connectionState.value)

                // Lancer l'activité de jeu après connexion réussie
                context.runOnUiThread {
                    Toast.makeText(context, "Connecté à ${device.name}", Toast.LENGTH_LONG).show()
                    startMultiplayerGame()
                }

            } catch (e: Exception) {
                Log.e("Bluetooth", "Erreur de connexion", e)
                connectionState.value = BluetoothState.ERROR
                connectionCallback?.invoke(connectionState.value)
            }
        }.start()
    }

    fun setConnectionCallback(callback: (BluetoothState) -> Unit) {
        this.connectionCallback = callback
    }

    // Ajoutez cette classe interne pour gérer la communication
    private inner class ConnectedThread(private val socket: BluetoothSocket) : Thread() {
        private val inputStream: InputStream = socket.inputStream
        private val outputStream: OutputStream = socket.outputStream
        private val buffer = ByteArray(1024)

        override fun run() {
            while (true) {
                try {
                    val bytes = inputStream.read(buffer)
                    val message = String(buffer, 0, bytes)
                    // Traiter les messages reçus
                    handleIncomingMessage(message)
                } catch (e: IOException) {
                    break
                }
            }
        }

        fun write(message: String) {
            try {
                outputStream.write(message.toByteArray())
            } catch (e: IOException) {
                Log.e("Bluetooth", "Erreur d'écriture", e)
            }
        }
    }

    // Pour envoyer des messages
    fun sendGameMessage(message: String) {
        connectedThread?.write(message)
    }

    // Pour recevoir des messages
    private fun handleIncomingMessage(message: String) {
        when (message) {
            "START_GAME" -> {
                context.runOnUiThread {
                    Toast.makeText(context, "Le jeu commence!", Toast.LENGTH_SHORT).show()
                    // Mettre à jour l'UI ou l'état du jeu
                }
            }
            // Ajoutez d'autres commandes de jeu ici
        }
    }

    fun startAdvertising() {
        try {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
                val advertiser = bluetoothAdapter.bluetoothLeAdvertiser

                val settings = AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .build()

                val data = AdvertiseData.Builder().setIncludeDeviceName(true).build()

                advertiseCallback = object : AdvertiseCallback() {
                    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
                        Toast.makeText(context, "Aspiration démarrée", Toast.LENGTH_SHORT).show()

                        advertiseTimer = object : CountDownTimer(60_000, 1_000) {
                            override fun onTick(millisUntilFinished: Long) {
                                val secondsLeft = millisUntilFinished / 1000
                                context.runOnUiThread {
                                    timerTextView.text = "Temps restant : ${secondsLeft}s"
                                    timerTextView.visibility = View.VISIBLE
                                }
                            }

                            override fun onFinish() {
                                stopAdvertising()
                                context.runOnUiThread {
                                    timerTextView.text = "Aspiration terminée"
                                }
                            }
                        }.start()
                    }

                    override fun onStartFailure(errorCode: Int) {
                        Toast.makeText(context, "Erreur d'aspiration : $errorCode", Toast.LENGTH_SHORT).show()
                    }
                }

                advertiser?.startAdvertising(settings, data, advertiseCallback)
            } else {
                Toast.makeText(context, "Permission d'aspiration Bluetooth manquante", Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission pour le mode aspirage", e)
        }
    }

    fun stopAdvertising() {
        bluetoothAdapter.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        advertiseTimer?.cancel()
        Toast.makeText(context, "Aspiration arrêtée", Toast.LENGTH_SHORT).show()
        Log.d("Bluetooth", "Aspiration arrêtée")

        context.runOnUiThread {
            timerTextView.visibility = View.GONE
        }
    }

    fun registerReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(discoveryReceiver, filter)
    }

    fun unregisterReceiver() {
        context.unregisterReceiver(discoveryReceiver)
    }

    private fun startMultiplayerGame() {
        val intent = Intent(context, MultiplayerGameActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}
