package com.jeckonly.aidldemo

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Process
import android.text.TextUtils
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.net.ServerSocket
import java.net.Socket

private const val TAG = "LocalSocketService"
class LocalSocketService : Service() {
    private var isServiceDestroyed = false

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            val serverSocket: ServerSocket = try {
                //监听8688端口
                withContext(Dispatchers.IO) {
                    ServerSocket(8080)
                }
            } catch (e: IOException) {
                return@launch
            }
            while (!isServiceDestroyed) {
                try {
                    // 接受客户端请求，并且阻塞直到接收到消息
                    val client =
                        withContext(Dispatchers.IO) {
                            serverSocket.accept()
                        }
                    withContext(Dispatchers.IO) {
                        try {
                            responseClient(client)
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        // TODO: Return the communication channel to the service.
        throw UnsupportedOperationException("Not yet implemented")
    }

    @Throws(IOException::class)
    private fun responseClient(client: Socket) {
        // 用于接收客户端消息
        val fromClient = BufferedReader(InputStreamReader(client.getInputStream()))
        // 用于向客户端发送消息
        val toClient = PrintWriter(BufferedWriter(OutputStreamWriter(client.getOutputStream())), true)
        toClient.println("您好，我是服务端")
        while (!isServiceDestroyed) {
            val str = fromClient.readLine()
            Log.i(TAG, "收到客户端发来的信息:$str")
            // NOTE 为什么为空就是断开连接
            if (str == "关闭") {
                //客户端断开了连接
                Log.i(TAG, "客户端断开连接")
                break
            }
            val message = "${str}_Server(${Process.myPid()})"
            // 从客户端收到的消息加工再发送给客户端
            toClient.println(message)
        }
        toClient.close()
        fromClient.close()
        client.close()
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceDestroyed = true
    }
}