package com.jeckonly.server

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class DefaultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default)

        startService(Intent(this, MyService::class.java))
        startService(Intent(this, MessengerService::class.java))

        findViewById<Button>(R.id.button1).setOnClickListener {
            sendBroadcast(Intent("com.jeckonly.aidldemo.data").apply {
                setPackage("com.jeckonly.aidldemo")
                putExtra("data", "data")
            })
        }
    }
}