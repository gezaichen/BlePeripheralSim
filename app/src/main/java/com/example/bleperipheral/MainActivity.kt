package com.example.bleperipheral

import android.Manifest
import android.bluetooth.BluetoothManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    private lateinit var bleManager: BlePeripheralManager
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var dataText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        dataText = findViewById(R.id.dataText)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ),
            1
        )

        val btManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val btAdapter = btManager.adapter
        bleManager = BlePeripheralManager(this, btAdapter)

        bleManager.onDataSent = { frame ->
            runOnUiThread {
                dataText.text = frame.joinToString(", ") { String.format("%.1f", it) }
            }
        }

        btnStart.setOnClickListener {
            bleManager.start()
            btnStart.isEnabled = false
            btnStop.isEnabled = true
            dataText.text = "开始发送..."
        }
        btnStop.setOnClickListener {
            bleManager.stop()
            btnStart.isEnabled = true
            btnStop.isEnabled = false
            dataText.text = "发送已停止"
        }
    }

    override fun onDestroy() {
        bleManager.stop()
        super.onDestroy()
    }
}
