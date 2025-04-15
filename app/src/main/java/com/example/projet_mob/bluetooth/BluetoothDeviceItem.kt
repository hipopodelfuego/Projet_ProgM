package com.example.projet_mob.bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

@Composable
fun BluetoothDeviceItem(
    device: BluetoothDevice,
    context: Context,
    bluetoothManager: BluetoothManager
) {
    val hasPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_CONNECT
    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                if (hasPermission) {
                    bluetoothManager.connectToDevice(device)
                } else {
                    Toast.makeText(context, "Permission Bluetooth manquante", Toast.LENGTH_SHORT).show()
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = if (hasPermission) device.name ?: "Inconnu" else "Permission requise")
        Text(text = if (hasPermission) device.address else "Permission requise")
    }
}
