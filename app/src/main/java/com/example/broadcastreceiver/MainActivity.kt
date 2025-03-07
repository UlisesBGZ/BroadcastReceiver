package com.example.broadcastreceiver

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.content.SharedPreferences
import android.util.Log

class MainActivity : AppCompatActivity() {
    private lateinit var phoneNumberEditText: EditText
    private lateinit var messageEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var prefs: SharedPreferences

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.READ_PHONE_STATE] == true &&
            permissions[Manifest.permission.SEND_SMS] == true &&
            permissions[Manifest.permission.READ_CALL_LOG] == true) {
            Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions required for auto-reply.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        phoneNumberEditText = findViewById(R.id.phoneNumberEditText)
        messageEditText = findViewById(R.id.messageEditText)
        saveButton = findViewById(R.id.saveButton)

        prefs = getSharedPreferences("AutoReplySettings", MODE_PRIVATE)
        loadSettings()

        saveButton.setOnClickListener {
            saveSettings()
        }

        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions.launch(arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_CALL_LOG
            ))
        }
    }

    private fun saveSettings() {
        val phoneNumber = normalizeNumber(phoneNumberEditText.text.toString())
        val message = messageEditText.text.toString()

        val editor = prefs.edit()
        editor.putString("phoneNumber", phoneNumber)
        editor.putString("message", message)
        editor.apply()

        Toast.makeText(this, "Saved Number: $phoneNumber, Message: $message", Toast.LENGTH_LONG).show()
        Log.d("MainActivity", "Number saved: $phoneNumber, Message saved: $message")
    }

    private fun loadSettings() {
        val savedPhoneNumber = prefs.getString("phoneNumber", "")
        val savedMessage = prefs.getString("message", "I'm busy, will call you back.")

        phoneNumberEditText.setText(savedPhoneNumber)
        messageEditText.setText(savedMessage)

        Log.d("MainActivity", "Loaded Number: $savedPhoneNumber, Loaded Message: $savedMessage")
    }

    private fun normalizeNumber(phoneNumber: String?): String {
        return phoneNumber?.replace("[^\\d]".toRegex(), "") ?: ""
    }
}