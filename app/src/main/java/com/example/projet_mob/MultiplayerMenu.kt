package com.example.projet_mob

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

class MultiplayerMenu : ComponentActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val discoveredDevices = mutableStateListOf<BluetoothDevice>()

    // Registering permission request launcher
    private val requestBluetoothPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (allGranted) {
                Toast.makeText(this, "Permissions Bluetooth accordées", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions Bluetooth refusées", Toast.LENGTH_SHORT).show()
            }
        }

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action: String? = intent?.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // Vérification des permissions avant de manipuler les appareils Bluetooth
                if (ContextCompat.checkSelfPermission(
                        context ?: return,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    val device: BluetoothDevice =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                    val deviceName = device.name ?: "Inconnu"
                    val deviceAddress = device.address // adresse MAC de l'appareil

                    // Ajoute le périphérique à la liste si il n'y est pas déjà
                    if (!discoveredDevices.contains(device)) {
                        discoveredDevices.add(device)
                    }

                    // Affiche les informations de l'appareil
                    Log.d("Bluetooth", "Appareil trouvé : $deviceName ($deviceAddress)")
                } else {
                    Log.w("Bluetooth", "Permission BLUETOOTH_CONNECT non accordée.")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        checkBluetoothPermissions()

        // Register the receiver for Bluetooth discovery
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(discoveryReceiver, filter)

        setContent {
            BluetoothScreen(
                isBluetoothEnabled = bluetoothAdapter.isEnabled,
                onEnableBluetooth = { enableBluetooth() },
                onStartDiscovery = { startDiscovery() },
                discoveredDevices = discoveredDevices
            )
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val message = if (result.resultCode == RESULT_OK) {
            "Bluetooth activé"
        } else {
            "Activation du Bluetooth refusée"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun enableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableBtIntent)
    }

    private fun checkBluetoothPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED }) {
            requestBluetoothPermissionsLauncher.launch(permissions)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startDiscovery() {
        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Bluetooth désactivé", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Permission BLUETOOTH_SCAN manquante", Toast.LENGTH_SHORT).show()
            requestBluetoothPermissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN))
            return
        }

        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
            Toast.makeText(this, "Arrêt de la découverte", Toast.LENGTH_SHORT).show()
        }

        // Start Bluetooth device discovery
        Toast.makeText(this, "Lancement de la recherche Bluetooth...", Toast.LENGTH_SHORT).show()
        discoveredDevices.clear()  // Clear previous devices

        val success = bluetoothAdapter.startDiscovery()

        if (!success) {
            Toast.makeText(this, "Échec du lancement de la découverte", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("Bluetooth", "Découverte en cours...")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the receiver to avoid memory leaks
        unregisterReceiver(discoveryReceiver)
    }
}

@Composable
fun BluetoothScreen(
    isBluetoothEnabled: Boolean,
    onEnableBluetooth: () -> Unit,
    onStartDiscovery: () -> Unit,
    discoveredDevices: List<BluetoothDevice>
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(text = if (isBluetoothEnabled) "Bluetooth est activé" else "Bluetooth est désactivé")
        Button(onClick = onEnableBluetooth) { Text("Activer Bluetooth") }
        Button(onClick = onStartDiscovery) { Text("Rechercher des appareils") }
        Text("Appareils détectés :")

        LazyColumn {
            items(discoveredDevices) { device ->
                BluetoothDeviceItem(device, context)
            }
        }
    }
}

@Composable
fun BluetoothDeviceItem(device: BluetoothDevice, context: Context) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_CONNECT
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = if (hasPermission) device.name ?: "Inconnu" else "Permission requise")
        Text(text = if (hasPermission) device.address else "Permission requise")
    }
}
