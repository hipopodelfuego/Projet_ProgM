package com.example.projet_mob.bluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.BroadcastReceiver
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

class BluetoothManager(val context: Activity) {

    val bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices = mutableStateListOf<BluetoothDevice>()

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

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device: BluetoothDevice? =
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                device?.let {
                    if (!discoveredDevices.contains(it)) {
                        discoveredDevices.add(it)
                    }
                }
            }
        }
    }

    init {
        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as android.bluetooth.BluetoothManager
        bluetoothAdapter = btManager.adapter
        registerReceiver()
    }

    // Vérification et demande de permissions Bluetooth
    fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            Log.d("Bluetooth", "Permissions manquantes : ${permissionsNeeded.joinToString()}")
            requestBluetoothPermissionsLauncher.launch(permissionsNeeded.toTypedArray())
        } else {
            Log.d("Bluetooth", "Toutes les permissions Bluetooth sont déjà accordées.")
        }
    }


    // Activer Bluetooth si nécessaire
    fun enableBluetooth() {
        try {
            if (!bluetoothAdapter.isEnabled) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    bluetoothAdapter.enable()
                    Toast.makeText(context, "Bluetooth activé", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Permission Bluetooth manquante", Toast.LENGTH_SHORT).show()
                    checkPermissions()
                }
            } else {
                Toast.makeText(context, "Bluetooth déjà activé", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission lors de l'activation de Bluetooth", e)
            Toast.makeText(context, "Erreur de permission Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    // Démarrer la découverte des appareils Bluetooth
    fun startDiscovery() {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                if (bluetoothAdapter.isDiscovering) {
                    bluetoothAdapter.cancelDiscovery()
                }
                discoveredDevices.clear()
                bluetoothAdapter.startDiscovery()
            } else {
                Toast.makeText(context, "Permission de scan manquante", Toast.LENGTH_SHORT).show()
                checkPermissions()
            }
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission lors de la découverte des appareils Bluetooth", e)
            Toast.makeText(context, "Erreur de permission Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    fun connectToDevice(device: BluetoothDevice) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "Permission Bluetooth manquante", Toast.LENGTH_SHORT).show()
            Log.e("Bluetooth", "Permission Bluetooth manquante pour connecter")
            checkPermissions()  // Demande de permissions
            return
        }

        try {
            // Vérifie les UUIDs disponibles pour le périphérique
            device.uuids?.let {
                if (it.isNotEmpty()) {
                    it.forEach { uuid ->
                        Log.d("Bluetooth", "UUID disponible: ${uuid.uuid}")
                    }
                } else {
                    Log.e("Bluetooth", "Aucun UUID disponible pour l'appareil ${device.name}")
                    Toast.makeText(context, "Aucun UUID disponible", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            val uuid: UUID = device.uuids[0].uuid // UUID du service Bluetooth de l'appareil
            val socket: BluetoothSocket = device.createRfcommSocketToServiceRecord(SPP_UUID)

            // Créer un thread pour la connexion
            Thread {
                try {
                    socket.connect()
                    Log.d("Bluetooth", "Connecté à ${device.name}")

                    // Lire/écrire des données après connexion...
                    val outputStream: OutputStream = socket.outputStream
                    val inputStream: InputStream = socket.inputStream
                    val message = "Hello, Bluetooth!"
                    outputStream.write(message.toByteArray())

                    val buffer = ByteArray(1024)
                    var bytes: Int
                    while (true) {
                        bytes = inputStream.read(buffer)
                        // Gérer les données reçues ici...
                    }
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Erreur de connexion", e)
                    Toast.makeText(context, "Erreur de connexion Bluetooth: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }.start()

        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission lors de la connexion", e)
            Toast.makeText(context, "Erreur de permission Bluetooth", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("Bluetooth", "Erreur lors de la création du socket", e)
            Toast.makeText(context, "Erreur inconnue lors de la connexion", Toast.LENGTH_SHORT).show()
        }catch (e: IOException) {
            Log.e("Bluetooth", "Erreur de connexion", e)
            Toast.makeText(context, "Erreur de connexion Bluetooth: ${e.message}", Toast.LENGTH_SHORT).show()
        }

    }


    // Écouter les connexions des clients Bluetooth
    fun startListening() {
        try {
            val serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord("MyBluetoothService", UUID.randomUUID())
            Thread {
                try {
                    val socket = serverSocket.accept()
                    Log.d("Bluetooth", "Client connecté : ${socket.remoteDevice.name}")

                    // Gestion de la connexion du client
                    val outputStream: OutputStream = socket.outputStream
                    val inputStream: InputStream = socket.inputStream
                    val message = "Hello from the server!"
                    outputStream.write(message.toByteArray())

                    val buffer = ByteArray(1024)
                    var bytes: Int
                    while (true) {
                        bytes = inputStream.read(buffer)
                        // Traiter les données reçues
                    }
                } catch (e: IOException) {
                    Log.e("Bluetooth", "Erreur de connexion serveur", e)
                }
            }.start()
        } catch (e: SecurityException) {
            Log.e("Bluetooth", "Erreur de permission lors de l'écoute des connexions", e)
            Toast.makeText(context, "Erreur de permission Bluetooth", Toast.LENGTH_SHORT).show()
        }
    }

    // Enregistrer le récepteur pour la découverte des appareils
    fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
        }
        context.registerReceiver(discoveryReceiver, filter)
    }

    // Désenregistrer le récepteur pour la découverte des appareils
    fun unregisterReceiver() {
        context.unregisterReceiver(discoveryReceiver)
    }

    // Vérifier si toutes les permissions Bluetooth sont accordées
    fun hasBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun getDiscoveredDevices(): List<BluetoothDevice> {
        return discoveredDevices
    }
}
