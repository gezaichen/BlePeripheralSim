package com.example.bleperipheral

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class BlePeripheralManager(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter,
    private val onSend: (ByteArray) -> Unit
) {
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("0000f00d-0000-1000-8000-00805f9b34fb")
        val CHAR_UUID: UUID = UUID.fromString("0000beef-0000-1000-8000-00805f9b34fb")
    }

    private var advertiser: BluetoothLeAdvertiser? = null
    private var gattServer: BluetoothGattServer? = null
    private val clients = mutableSetOf<BluetoothDevice>()
    private var packetCount = 0L
    private val handler = Handler(Looper.getMainLooper())
    private val intervalMs = 16L // ~60Hz

    private val gattCallback = object : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) clients.add(device)
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) clients.remove(device)
        }

        override fun onDescriptorWriteRequest(
            device: BluetoothDevice, requestId: Int, descriptor: BluetoothGattDescriptor,
            preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray
        ) {
            if (descriptor.uuid == UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")) {
                if (responseNeeded) {
                    gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                }
            }
        }
    }

    fun start() {
        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .build()
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        advertiser?.startAdvertising(settings, data, object : AdvertiseCallback() {})

        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        gattServer = btManager.openGattServer(context, gattCallback)

        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        val charac = BluetoothGattCharacteristic(
            CHAR_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        val desc = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        charac.addDescriptor(desc)
        service.addCharacteristic(charac)
        gattServer?.addService(service)

        handler.post(sendRunnable)
    }

    fun stop() {
        handler.removeCallbacks(sendRunnable)
        gattServer?.close()
        advertiser?.stopAdvertising(object : AdvertiseCallback() {})
    }

    private val sendRunnable = object : Runnable {
        override fun run() {
            packetCount++
            val frame = FloatArray(526)
            frame[0] = packetCount.toFloat()
            frame[1] = System.currentTimeMillis().toFloat()
            for (i in 0 until 512) {
                frame[2 + i] = (Math.random() * 1000).toFloat()
            }
            for (i in 0 until 6) frame[514 + i] = (Math.random() * 10).toFloat()
            for (i in 0 until 6) frame[520 + i] = (Math.random() * 180).toFloat()

            val buf = ByteBuffer.allocate(526 * 4).order(ByteOrder.LITTLE_ENDIAN)
            frame.forEach { buf.putFloat(it) }
            val bytes = buf.array()

            val charac = gattServer?.getService(SERVICE_UUID)?.getCharacteristic(CHAR_UUID)
            clients.forEach { dev ->
                gattServer?.notifyCharacteristicChanged(dev, charac, false)
            }
            onSend(bytes)
            handler.postDelayed(this, intervalMs)
        }
    }
}
