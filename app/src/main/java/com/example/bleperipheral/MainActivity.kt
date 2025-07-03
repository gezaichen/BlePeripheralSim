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

        val btAdapter = BluetoothAdapter.getDefaultAdapter()
        // ❌ 不再强制设置名称，避免权限异常

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        dataTextView = findViewById(R.id.dataTextView)

        blePeripheralManager = BlePeripheralManager(this, btAdapter) { bytes ->
            runOnUiThread {
                val hex = bytes.take(20).joinToString(" ") { String.format("%02X", it) }
                dataTextView.text = "\uD83D\uDCCA 正在发送数据 (前20字节):\n$hex"
            }
        }

        startButton.setOnClickListener {
            blePeripheralManager.start()
            dataTextView.text = "\u2705 蓝牙广播已启动"
        }

        stopButton.setOnClickListener {
            blePeripheralManager.stop()
            dataTextView.text = "\u23F9\uFE0F 蓝牙广播已停止"
        }
    }
}
