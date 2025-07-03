package com.example.bleperipheral

import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
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

        // ✅ Android 12+ 需要动态请求 BLUETOOTH_CONNECT 权限，避免设置名称时报错
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT), 1)
        } else {
            // 可选：设置蓝牙名称（不设置也不会影响广播）
            // btAdapter.name = "BLEPeripheralSimulator"
        }

        startButton = findViewById(R.id.startButton)
        stopButton = findViewById(R.id.stopButton)
        dataTextView = findViewById(R.id.dataTextView)

        blePeripheralManager = BlePeripheralManager(this, btAdapter) { bytes ->
            runOnUiThread {
                val hex = bytes.take(20).joinToString(" ") { String.format("%02X", it) }
                dataTextView.text = "📊 正在发送数据 (前20字节):\n$hex"
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

    // 可选：动态权限回调
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            BluetoothAdapter.getDefaultAdapter()?.name = "BLEPeripheralSimulator"
        }
    }
}
