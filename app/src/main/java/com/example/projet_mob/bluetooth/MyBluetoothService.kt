package com.example.projet_mob.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.bluetooth.BluetoothManager
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
import androidx.core.content.ContextCompat
import java.io.IOException
import java.util.*
import com.example.projet_mob.MultiplayerGameActivity
import com.example.projet_mob.MultiplayerGameState

val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class MyBluetoothService(
    val context: Activity,
    private val timerTextView: TextView
) {
    val bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices = mutableStateListOf<BluetoothDevice>()
    private var deviceFoundCallback: ((String, String) -> Unit)? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseTimer: CountDownTimer? = null
    var onConnectedCallback: (() -> Unit)? = null
    var onMessageReceivedCallback: ((String) -> Unit)? = null

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!discoveredDevices.contains(it)) {
                        discoveredDevices.add(it)
                        deviceFoundCallback?.invoke(it.name ?: "Unknown", it.address)
                    }
                }
            }
        }
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
    private var gameStarted = false


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
            // Vérifie si la permission pour scanner les appareils est accordée
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                if (bluetoothAdapter.isDiscovering) bluetoothAdapter.cancelDiscovery()
                discoveredDevices.clear()
                bluetoothAdapter.startDiscovery()
            } else {
                Toast.makeText(context, "Permission de scan manquante", Toast.LENGTH_SHORT).show()
                checkBluetoothPermissions()  // Vérifier et demander les permissions
            }
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission lors de la découverte", e)
        }
    }


    fun setOnDeviceFoundListener(callback: (String, String) -> Unit) {
        this.deviceFoundCallback = callback
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permission Bluetooth manquante", Toast.LENGTH_SHORT).show()
            checkPermissions()
            return
        }

        val socket: BluetoothSocket?
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
        } catch (e: IOException) {
            Log.e("Bluetooth", "Erreur lors de la création du socket", e)
            return
        }

        Thread {
            try {
                bluetoothAdapter.cancelDiscovery()
                socket.connect()
                connectedSocket = socket
                onConnectedCallback?.invoke()

// Ajouter gestion des messages entrants
                Thread {
                    val inputStream = socket.inputStream
                    val buffer = ByteArray(1024)
                    while (true) {
                        val bytes = inputStream.read(buffer)
                        if (bytes > 0) {
                            val message = String(buffer, 0, bytes)
                            Log.d("Bluetooth", "Message reçu (client) : $message")
                            handleMessage(message)
                        }
                    }
                }.start()

                context.runOnUiThread {
                    Toast.makeText(context, "Connecté à ${device.name}", Toast.LENGTH_SHORT).show()
                }
                Log.d("Bluetooth", "Connecté à ${device.name}")

            } catch (e: IOException) {
                Log.e("Bluetooth", "Erreur de connexion", e)
                context.runOnUiThread {
                    Toast.makeText(context, "Échec de connexion à ${device.name}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    fun startListeningForConnections() {
        val serverSocket: BluetoothServerSocket? = bluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothGame", SPP_UUID)
        Thread {
            try {
                val socket = serverSocket?.accept() // Attendre la connexion entrante
                if (socket != null) {
                    Log.d("Bluetooth", "Connexion acceptée avec ${socket.remoteDevice.name}")
                    acceptConnection(socket)
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Erreur de connexion", e)
            }
        }.start()
    }

    fun acceptConnection(socket: BluetoothSocket) {
        connectedSocket = socket

        Thread {
            val inputStream = socket.inputStream
            val buffer = ByteArray(1024)

            try {
                while (true) {
                    val bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        val message = String(buffer, 0, bytesRead)
                        Log.d("Bluetooth", "Message brut reçu: $message")

                        // Traiter chaque message séparément
                        message.split("\n").forEach { singleMessage ->
                            if (singleMessage.isNotEmpty()) {
                                Log.d("Bluetooth", "Traitement du message: $singleMessage")
                                handleMessage(singleMessage)
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("Bluetooth", "Erreur de lecture", e)
            }
        }.start()
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

    fun checkBluetoothPermissions() {
        // Permissions Bluetooth nécessaires
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

        // Vérifier si les permissions sont accordées
        val neededPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        // Si des permissions sont manquantes, demander la permission
        if (neededPermissions.isNotEmpty()) {
            Log.d("Bluetooth", "Permissions manquantes : ${neededPermissions.joinToString()}")
            requestBluetoothPermissionsLauncher.launch(neededPermissions.toTypedArray())
        } else {
            Log.d("Bluetooth", "Toutes les permissions Bluetooth sont déjà accordées.")
        }
    }

    private var connectedSocket: BluetoothSocket? = null

    fun sendMessage(message: String) {
        try {
            connectedSocket?.outputStream?.apply {
                write("$message\n".toByteArray()) // Ajout du \n comme séparateur
                flush()
            }
            Log.d("Bluetooth", "Message envoyé: $message")
        } catch (e: IOException) {
            Log.e("Bluetooth", "Erreur lors de l'envoi du message", e)
        }
    }

    private fun handleMessage(message: String) {
        when {
            message.startsWith("START_GAME:") -> {
                if (gameStarted) {
                    return
                }

                gameStarted = true
                val ids = message.removePrefix("START_GAME:").split(",").mapNotNull { it.toIntOrNull() }
                val intent = Intent(context, MultiplayerGameActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.putExtra("challenge_ids", ids.toIntArray())
                context.startActivity(intent)

                Log.d("Bluetooth", "Démarrage du jeu avec les ids : ${ids.joinToString()}")
                sendMessage("START_GAME:${ids.joinToString(",")}")
            }

            message.startsWith("SCORE:") -> {
                val score = message.removePrefix("SCORE:").toIntOrNull() ?: return
                Log.d("Bluetooth", "Score reçu: $score")

                context.runOnUiThread {
                    MultiplayerGameState.opponentScore = score
                    Toast.makeText(
                        context,
                        "Score adversaire: $score",
                        Toast.LENGTH_LONG
                    ).show()

                    (context as? MultiplayerGameActivity)?.tryFinishGame()
                }
            }

            message == "WINNER" -> {
                MultiplayerGameActivity.setVictory(true)
            }

            message == "LOSER" -> {
                MultiplayerGameActivity.setVictory(false)
            }
        }
    }


}
