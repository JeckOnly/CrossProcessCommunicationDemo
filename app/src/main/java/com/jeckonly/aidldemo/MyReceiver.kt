package com.jeckonly.aidldemo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

private const val TAG = "MyReceiver"
class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        when (intent.action) {
            "com.jeckonly.aidldemo.data" -> {
                Log.d(TAG, intent.getStringExtra("data")?:"")
            }
            else -> {
                Log.d(TAG, "else")
            }
        }
    }
}