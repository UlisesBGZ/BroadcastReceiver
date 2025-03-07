package com.example.broadcastreceiver

import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.JobIntentService

class AutoReplyJobService : JobIntentService() {

    companion object {
        private const val JOB_ID = 1001
        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, AutoReplyJobService::class.java, JOB_ID, work)
        }
    }

    override fun onHandleWork(intent: Intent) {
        val incomingNumber = normalizeNumber(intent.getStringExtra("incomingNumber"))
        val prefs = getSharedPreferences("AutoReplySettings", Context.MODE_PRIVATE)
        val savedPhoneNumber = normalizeNumber(prefs.getString("phoneNumber", ""))
        val savedMessage = prefs.getString("message", "I'm busy, will call you back.") ?: "I'm busy, will call you back." // ✅ Load saved message

        Log.d("AutoReplyJobService", "Processing call from: $incomingNumber")
        Log.d("AutoReplyJobService", "Saved number: $savedPhoneNumber")
        Log.d("AutoReplyJobService", "Saved message: $savedMessage") // ✅ Log saved message

        if (incomingNumber == savedPhoneNumber) {
            sendSMS(incomingNumber, savedMessage) // ✅ Use saved message
        } else {
            Log.e("AutoReplyJobService", "Incoming number does not match saved number.")
        }
    }

    private fun sendSMS(phoneNumber: String, message: String) { // ✅ Message parameter
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null) // ✅ Use message parameter
            Log.d("AutoReplyJobService", "Auto-reply SMS sent to: $phoneNumber with message: $message") // ✅ Log message sent
        } catch (e: Exception) {
            Log.e("AutoReplyJobService", "SMS sending failed: ${e.message}")
        }
    }

    private fun normalizeNumber(phoneNumber: String?): String {
        return phoneNumber?.replace("[^\\d]".toRegex(), "") ?: ""
    }
}