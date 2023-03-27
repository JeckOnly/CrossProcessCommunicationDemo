package com.jeckonly.aidldemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.jeckonly.aidldemo.databinding.ActivityMainBinding
import com.jeckonly.api.IMyAidlInterface

const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {

    private lateinit var iMyAidlInterface: IMyAidlInterface

    private lateinit var binding: ActivityMainBinding

    private val serviceConnection: ServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected")
            Log.d(TAG, "${android.os.Process.myPid()}")
            iMyAidlInterface = IMyAidlInterface.Stub.asInterface(service)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected")
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)

        val intent = Intent().apply {
            component = ComponentName("com.jeckonly.server", "com.jeckonly.server.MyService")
        }
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        binding.button.setOnClickListener {
            binding.textView.text =
                iMyAidlInterface.doubleYourString(binding.textView.text.toString())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}