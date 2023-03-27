package com.jeckonly.server

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jeckonly.api.IMyAidlInterface

const val TAG = "MyService"
class MyService: Service() {
    override fun onBind(intent: Intent?): IBinder {
        return object :IMyAidlInterface.Stub() {
            override fun doubleYourString(data: String?): String {
                Log.d(TAG, "${android.os.Process.myPid()}")
                return "$data$data"
            }
        }
    }
}