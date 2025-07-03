package com.example.bleperipheral

import android.bluetooth.BluetoothAdapter
import android.content.Context
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
        btAdapter.name = "BLEPeripheralSimulator" // 设置广播名称

        blePeripheralManager = BlePeripheralManager(this, btAdapter) { bytes ->
            runOnUiThread {
                val hex = bytes.take(32).joinToString(" ") { String.format("%02X", it) } + " ..."
                dataTextView.append("\n📤 发送 ${bytes.size} 字节: $hex")
                if (dataTextView.lineCount > 200) {
                    dataTextView.text = dataTextView.text.split("\n").takeLast(200).joinToString("\n")
                }
            }
        }

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        dataTextView = findViewById(R.id.dataTextView)

        startButton.setOnClickListener {
            blePeripheralManager.start()
            dataTextView.append("\n▶️ 开始广播并发送数据...")
        }

        stopButton.setOnClickListener {
            blePeripheralManager.stop()
            dataTextView.append("\n⏹️ 停止发送")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blePeripheralManager.stop()
    }
}
