package com.example.projet_mob.bluetooth

import android.bluetooth.BluetoothDevice
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
    onStartAdvertising: () -> Unit,  // Fonction pour activer le mode aspirage
    discoveredDevices: List<BluetoothDevice>,
    myBluetoothService: MyBluetoothService
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bluetooth Devices")

        if (isBluetoothEnabled) {
            // Bouton pour démarrer la découverte
            Button(onClick = { onStartDiscovery() }) {
                Text("Start Discovery")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bouton pour activer le mode aspirage
            Button(onClick = { onStartAdvertising() }) {
                Text("Enable Advertising (Aspirage Mode)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Affichage des appareils Bluetooth découverts
            LazyColumn {
                items(discoveredDevices) { device ->
                    BluetoothDeviceItem(device, myBluetoothService.context, myBluetoothService) // Pass context
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

        } else {
            Button(onClick = { onEnableBluetooth() }) {
                Text("Enable Bluetooth")
            }
        }
    }
}
