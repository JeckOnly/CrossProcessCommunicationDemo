package com.jeckonly.aidldemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.jeckonly.aidldemo.databinding.ActivityMainBinding
import com.jeckonly.api.IMyAidlInterface
import com.jeckonly.api.model.User


private const val TAG = "MainActivity"
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

    private val messengerServiceConnection: ServiceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "messengerServiceConnection： onServiceConnected")
            Log.d(TAG, "${android.os.Process.myPid()}")
            messenger = Messenger(service)
            val bundle = Bundle().apply {
                putString("client_data", "client_data")
            }
            val message = Message.obtain(null, 1).apply {
                data = bundle
                replyTo = Messenger(dealServerMessageHandler)
            }
            try {
                messenger.send(message)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "messengerServiceConnection： onServiceDisconnected")
        }

    }

    private val handlerThread = HandlerThread("MainActivity").apply {
        start()
    }

    private val dealServerMessageHandler = Handler(handlerThread.looper) {
        // override callback进行了处理就不需要override handleMessage
        when (it.what) {
            2 -> {
                Log.d(TAG, "default bundle classloader: ${it.data.classLoader}")
                it.data.classLoader = classLoader
                Log.d(TAG, "changed bundle classloader: ${it.data.classLoader}")
                Log.d(TAG, ("Messenger Handler   " + it.data.getString("server_data")))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Log.d(TAG, ("Messenger Handler getServerUser " + it.data.getParcelable("server_user", User::class.java)))
                }else {
                    Log.d(TAG, ("Messenger Handler getServerUser " + it.data.getParcelable("server_user")))
                }

            }
            else -> {
                Log.d(TAG,"else")
            }
        }
        return@Handler true
    }

    private lateinit var messenger: Messenger

    private var serviceConnectionList = mutableListOf<ServiceConnection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)

        val intent = Intent().apply {
            component = ComponentName("com.jeckonly.server", "com.jeckonly.server.MyService")
        }
        val bindMessengerIntent = Intent().apply {
            component = ComponentName("com.jeckonly.server", "com.jeckonly.server.MessengerService")
        }
        serviceConnection.apply {
            bindService(intent, this, Context.BIND_AUTO_CREATE)
            serviceConnectionList.add(this)
        }
        messengerServiceConnection.apply {
            bindService(bindMessengerIntent, this, Context.BIND_AUTO_CREATE)
            serviceConnectionList.add(this)
        }
        binding.button.setOnClickListener {
            binding.textView.text =
                iMyAidlInterface.doubleYourString(binding.textView.text.toString())
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        serviceConnectionList.forEach {
            unbindService(it)
        }
    }


}