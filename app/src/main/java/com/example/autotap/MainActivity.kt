package com.example.autotap

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings as SystemSettings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var intervalInput: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intervalInput = findViewById(R.id.intervalInput)
        statusText = findViewById(R.id.statusText)

        intervalInput.setText(Settings.getIntervalMs(this).toString())

        findViewById<Button>(R.id.saveButton).setOnClickListener { saveInterval() }
        findViewById<Button>(R.id.openSettingsButton).setOnClickListener {
            startActivity(Intent(SystemSettings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        statusText.setText(
            if (AutoTapService.instance != null) R.string.status_on else R.string.status_off
        )
    }

    private fun saveInterval() {
        val value = intervalInput.text.toString().toLongOrNull()
        if (value == null || value < Settings.MIN_INTERVAL_MS) {
            Toast.makeText(
                this,
                "${Settings.MIN_INTERVAL_MS} 以上の数値を入力してください",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        Settings.setIntervalMs(this, value)
        AutoTapService.instance?.onIntervalChanged()
        Toast.makeText(this, "保存しました（${value} ミリ秒）", Toast.LENGTH_SHORT).show()
    }
}
