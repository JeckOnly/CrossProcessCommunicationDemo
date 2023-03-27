package com.jeckonly.server

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log
import com.jeckonly.api.model.User

private const val TAG = "MessengerService"

class MessengerService : Service() {

    private val handlerThread = HandlerThread("MessengerService").apply {
        start()
    }

    private val dealClientMessageHandler = Handler(handlerThread.looper) {
        // override callback进行了处理就不需要override handleMessage
        when (it.what) {
            1 -> {
                Log.d(TAG, ("Messenger Handler   " + it.data.getString("client_data")))
                val messenger = it.replyTo
                val bundle = Bundle().apply {
                    putString("server_data", "server_data")
                    putParcelable("server_user", User("Server", 1))
                }
                val message = Message.obtain(null, 2).apply {
                    data = bundle
                }
                try {
                    messenger.send(message)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }

            }
            else -> {
                Log.d(TAG,"else")
            }
        }
        return@Handler true
    }

    override fun onBind(intent: Intent): IBinder {
         return Messenger(dealClientMessageHandler).binder
    }
}