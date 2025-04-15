package com.example.projet_mob

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.projet_mob.bluetooth.BluetoothManager
import com.example.projet_mob.bluetooth.BluetoothScreen

class MultiplayerMenu : ComponentActivity() {
    private lateinit var bluetoothManager: BluetoothManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothManager = BluetoothManager(this)
        bluetoothManager.checkPermissions()

        setContent {
            BluetoothScreen(
                isBluetoothEnabled = bluetoothManager.bluetoothAdapter.isEnabled,
                onEnableBluetooth = { bluetoothManager.enableBluetooth() },
                onStartDiscovery = { bluetoothManager.startDiscovery() },
                discoveredDevices = bluetoothManager.getDiscoveredDevices(),
                bluetoothManager = bluetoothManager
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.unregisterReceiver()
    }
}