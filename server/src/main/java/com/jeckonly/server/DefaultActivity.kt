package com.jeckonly.server

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class DefaultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default)

        startService(Intent(this, MyService::class.java))
    }
}