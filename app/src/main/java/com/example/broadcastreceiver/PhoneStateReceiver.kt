package com.example.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import kotlinx.coroutines.*

class PhoneStateReceiver : BroadcastReceiver() {

    private var previousState = TelephonyManager.EXTRA_STATE_IDLE
    private var isRinging = false
    private var ringingStartTime: Long = 0
    private val rejectionDetectionDelayMillis = 500L // ✅ Short delay (500ms) for rejection detection
    private var coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val currentState = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            Log.d("PhoneStateReceiver", "State changed Previous: $previousState, Current: $currentState, Incoming Number: $incomingNumber, isRinging: $isRinging")

            if (currentState == TelephonyManager.EXTRA_STATE_RINGING) {
                // ✅ Phone just started ringing - start delayed rejection check
                isRinging = true
                ringingStartTime = System.currentTimeMillis()
                Log.d("PhoneStateReceiver", "Incoming call RINGING from: $incomingNumber - Starting rejection delay timer")

                coroutineScope.launch { // ✅ Launch coroutine for delay
                    delay(rejectionDetectionDelayMillis) // ✅ Short delay

                    val currentPhoneStateAfterDelay = intent.getStringExtra(TelephonyManager.EXTRA_STATE) // Get state again AFTER delay
                    Log.d("PhoneStateReceiver", "Delayed check - State after delay: $currentPhoneStateAfterDelay")

                    if (currentPhoneStateAfterDelay != TelephonyManager.EXTRA_STATE_OFFHOOK) {
                        // ✅ After delay, state is NOT OFFHOOK - likely rejected or missed
                        Log.d("PhoneStateReceiver", "Likely call REJECTED/MISSED (after delay) from: $incomingNumber - Sending auto-reply")
                        if (!incomingNumber.isNullOrBlank()) {
                            val serviceIntent = Intent(context, AutoReplyJobService::class.java)
                            serviceIntent.putExtra("incomingNumber", incomingNumber)
                            AutoReplyJobService.enqueueWork(context, serviceIntent)
                        }
                    } else {
                        // ✅ State is OFFHOOK - call was answered
                        Log.d("PhoneStateReceiver", "Call ANSWERED (OFFHOOK after delay) - NOT sending auto-reply")
                    }
                    isRinging = false // Reset ringing flag in coroutine
                }

            } else if (currentState == TelephonyManager.EXTRA_STATE_IDLE) {
                Log.d("PhoneStateReceiver", "Phone became IDLE - Resetting ringing flag")
                isRinging = false // Reset ringing flag if state becomes IDLE (e.g., call ended after being answered, or missed without ringing detection)


            } else if (currentState == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                // ✅ Call answered
                Log.d("PhoneStateReceiver", "Call answered - NOT sending auto-reply")
                isRinging = false // Reset ringing flag
            }

            previousState = currentState ?: TelephonyManager.EXTRA_STATE_IDLE
        }
    }
}