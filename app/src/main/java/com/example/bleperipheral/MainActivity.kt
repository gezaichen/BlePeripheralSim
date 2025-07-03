// ✅ MainActivity.kt
package com.example.bleperipheral

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var blePeripheralManager: BlePeripheralManager
    private lateinit var startButton: Button
    private lateinit var stopButton: Button
    private lateinit var dataTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        dataTextView = findViewById(R.id.dataTextView)

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        // ❌ 不再设置 btAdapter.name，避免权限问题

        blePeripheralManager = BlePeripheralManager(this, btAdapter) { bytes ->
            runOnUiThread {
                val hex = bytes.take(20).joinToString(" ") { String.format("%02X", it) }
                dataTextView.text = "\uD83D\uDCCA 正在发送数据 (前20字节):\n$hex"
            }
        }

        startButton.setOnClickListener {
            blePeripheralManager.start()
            dataTextView.text = "✅ 蓝牙广播已启动，开始发送数据"
        }

        stopButton.setOnClickListener {
            blePeripheralManager.stop()
            dataTextView.text = "⏹️ 蓝牙广播已停止"
        }
    }
}
