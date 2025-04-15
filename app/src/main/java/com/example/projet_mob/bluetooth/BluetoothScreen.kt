package com.example.projet_mob.bluetooth

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun BluetoothScreen(
    isBluetoothEnabled: Boolean,
    onEnableBluetooth: () -> Unit,
    onStartDiscovery: () -> Unit,
    discoveredDevices: List<BluetoothDevice>,
    bluetoothManager: BluetoothManager
) {
    val hasBluetoothPermissions = bluetoothManager.hasBluetoothPermissions() // Vérification des permissions

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bluetooth Devices")

        if (isBluetoothEnabled) {
            Button(onClick = { onStartDiscovery() }) {
                Text("Start Discovery")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage des appareils Bluetooth découverts
            LazyColumn {
                items(discoveredDevices) { device ->
                    BluetoothDeviceItem(device, bluetoothManager.context, bluetoothManager) // Pass context
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Démarrer l'écoute (mode serveur)
            Button(onClick = { bluetoothManager.startListening() }) {
                Text("Start Listening (Server Mode)")
            }

        } else {
            Button(onClick = { onEnableBluetooth() }) {
                Text("Enable Bluetooth")
            }
        }
    }
}
