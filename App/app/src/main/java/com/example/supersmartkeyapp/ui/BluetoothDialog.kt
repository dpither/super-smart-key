package com.example.supersmartkeyapp.ui

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.supersmartkeyapp.R
import com.example.supersmartkeyapp.ui.theme.AppTheme
import kotlin.math.max


private const val SCAN_PERIOD: Long = 10000
private const val TAG: String = "BLUETOOTH DIALOG"

@SuppressLint("MissingPermission", "NewApi")
@Composable
fun BluetoothDialog(
    onDismiss: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    val context = LocalContext.current
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter = bluetoothManager.adapter

    if (bluetoothAdapter == null) {
        ErrorDialog(onDismiss)
        return
    }

    val bleDevices = remember { mutableStateListOf<BluetoothDevice>() }
    val rssis = remember { mutableStateMapOf<BluetoothDevice, Int>() }
    val isScanning = remember { mutableStateOf(false) }
    val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!bleDevices.contains(result.device)) {
                bleDevices.add(result.device)
                rssis[result.device] = result.rssi
            } else {
                rssis[result.device] = max(result.rssi, rssis[result.device]!!)
            }
        }
    }

    fun scan() {
        if (!isScanning.value) {
            Handler(Looper.getMainLooper()).postDelayed({
                isScanning.value = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)
            isScanning.value = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            isScanning.value = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }


    val testGatt = object :BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG,"SERVICES DISCOVERED")
                gatt.services.forEach { service ->
                    Log.d(TAG, "UUID Version: ${service.uuid.version()} UUID: ${service.uuid} LSB: ${service.uuid.leastSignificantBits} MSB: ${service.uuid.mostSignificantBits}")
                }
                Log.d(TAG, "NAME?: ${gatt.device.name}")
            }
            else{
                Log.e(TAG,"Status: $status")
            }
        }
    }


    LaunchedEffect(Unit) {
        bleDevices.clear()
        bluetoothManager.getConnectedDevices(BluetoothProfile.GATT).forEach {
            Log.d(TAG, "Address: ${it.address}")
            Log.d(TAG, "Alias: ${it.alias}")
            Log.d(TAG, "Bluetooth Major Device Class: ${it.bluetoothClass.majorDeviceClass}")
            Log.d(TAG, "Bluetooth Device Class: ${it.bluetoothClass.deviceClass}")
            Log.d(TAG, "Bond State: ${it.bondState}")
            Log.d(TAG, "Name: ${it.name}")
            Log.d(TAG, "Type: ${it.type}")
            try {
                it.uuids.forEach { uuid ->
                    Log.d(TAG, "UUID: $uuid")
                }
            } catch (e: Exception) {
                Log.e(TAG, "$e")
//                it.connectGatt(context, false, testGatt)
//                val wut = it.fetchUuidsWithSdp()
//                Log.d(TAG, "Wut: $wut")
            }

            bleDevices.add(it)
            rssis[it] = 0
        }
//        scan()
    }
    ScanDialog(
        devices = bleDevices,
        rssis = rssis,
        isScanning = isScanning.value,
        onDismiss = onDismiss,
        onDeviceSelected = onDeviceSelected
    )
}

@SuppressLint("MissingPermission")
@Composable
private fun ScanDialog(
    devices: List<BluetoothDevice>,
    rssis: Map<BluetoothDevice, Int>,
    isScanning: Boolean,
    onDismiss: () -> Unit,
    onDeviceSelected: (BluetoothDevice) -> Unit
) {
    val scrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "bluetooth_title",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = if (isScanning) {
                            "bluetooth_scanning_text"
                        } else {
                           "bluetooth_default_text"
                        },
                    )
                }
                Spacer(modifier = Modifier.size(4.dp))
                Column(
                    modifier = Modifier
                        .size(300.dp)
                        .verticalScroll(scrollState)

                ) {
                    devices.forEach { device ->
                        ScanRow(name = device.name,
                            address = device.address,
                            rssi = rssis[device]!!,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Log.d("BLUETOOTH DIALOG", "Clicked $device")
                                    onDeviceSelected(device)
                                    onDismiss()
                                }
                        )
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
private fun ScanRow(
    name: String?,
    address: String,
    rssi: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(4.dp),
    ) {
        if (name != null) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = address,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = "RSSI: $rssi dBm",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun ErrorDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.no_bluetooth_title),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
                Text(
                    text = stringResource(R.string.no_bluetooth_text),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp),
                )
                TextButton(
                    onClick = { onDismiss() },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("OK")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScanDialog() {
    AppTheme {
        ScanDialog(
            devices = listOf(),
            rssis = mapOf(),
            isScanning = true,
            onDismiss = {},
            onDeviceSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewErrorDialog() {
    AppTheme {
        ErrorDialog(
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScanRows() {
    AppTheme {
        Column(
            modifier = Modifier
                .size(300.dp)

        ) {
            ScanRow(
                name = null,
                address = "16:36:24:DD:8E:DC",
                rssi = -30,
            )
            ScanRow(
                name = "yo mama",
                address = "16:36:24:DD:8E:DC",
                rssi = -30,
            )
        }
    }
}