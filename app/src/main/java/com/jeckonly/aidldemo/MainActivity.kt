package com.jeckonly.aidldemo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.jeckonly.aidldemo.databinding.ActivityMainBinding
import com.jeckonly.api.IMyAidlInterface
import com.jeckonly.api.model.Address
import com.jeckonly.api.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.Socket


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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Log.d(TAG, ("Messenger Handler getServerAddress " + it.data.getParcelable("server_address", Address::class.java)))
                }else {
                    Log.d(TAG, ("Messenger Handler getServerAddress " + it.data.getParcelable("server_address")))
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

    private lateinit var clientSocket: Socket
    private lateinit var printWriter: PrintWriter
    private lateinit var serverInput: BufferedReader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView<ActivityMainBinding?>(this, R.layout.activity_main)

        val intent = Intent().apply {
            component = ComponentName("com.jeckonly.server", "com.jeckonly.server.MyService")
        }
        val bindMessengerIntent = Intent().apply {
            component = ComponentName("com.jeckonly.server", "com.jeckonly.server.MessengerService")
        }
        val socketServiceIntent = Intent(this, LocalSocketService::class.java)
//        val socketServiceIntent = Intent().apply {
//            component = ComponentName("com.jeckonly.server", "com.jeckonly.server.SocketService")
//        }
        serviceConnection.apply {
            bindService(intent, this, Context.BIND_AUTO_CREATE)
            serviceConnectionList.add(this)
        }
        messengerServiceConnection.apply {
            bindService(bindMessengerIntent, this, Context.BIND_AUTO_CREATE)
            serviceConnectionList.add(this)
        }
        startService(socketServiceIntent)
        binding.button.setOnClickListener {
            binding.textView.text =
                iMyAidlInterface.doubleYourString(binding.textView.text.toString())
        }
        lifecycleScope.launch(Dispatchers.IO) {
            connectSocketServer()
        }

    }

    private suspend fun connectSocketServer() {
        Log.d(TAG, "开始连接")
        var socket: Socket? = null
        while (socket == null) {
            try {
                //选择和服务器相同的端口8688
                socket = withContext(Dispatchers.IO) {
                    Socket("localhost", 8080)
                }
                clientSocket = socket
                printWriter = withContext(Dispatchers.IO) {
                    PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                }
            } catch (e: Exception) {
                delay(5000)
                Log.e(TAG, e.printStackTrace().toString())

            }
        }
        try {
            // 接收服务器端的消息
            serverInput = withContext(Dispatchers.IO){
                BufferedReader(InputStreamReader(socket.getInputStream()))
            }

            while (!isFinishing) {
                val msg = withContext(Dispatchers.IO) {
                    serverInput.readLine()
                }
                if (msg != null) {
                    Log.i(TAG, "收到服务端的消息: $msg")
                    delay(1000)
                    printWriter.println("${msg}_client(${Process.myPid()})")
                }
            }
            withContext(Dispatchers.IO) {
                Log.d(TAG, "执行close")
                printWriter.close()
                serverInput.close()
                clientSocket.close()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceConnectionList.forEach {
            unbindService(it)
        }
        // 如果协程取消的时候没来得及关闭再次关闭
        printWriter.close()
        serverInput.close()
        clientSocket.close()
    }
}