package com.dmitryzenevich.remoteaudiocontrol.data

import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.Closeable
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketSource @Inject constructor() : Closeable {

    private var socket: Socket? = null
    private val mutex = Mutex()

    val writer
        get() = socket().getOutputStream().writer()

    val reader
        get() = socket().getInputStream().bufferedReader()

    private fun socket(): Socket = socket ?: throw IllegalStateException("Socket is not created")

    suspend fun openSocketConnection(address: String, port: Int) {
        mutex.withLock {
            close()
            socket = Socket().apply { connect(InetSocketAddress(address, port), 5000) }
        }
    }

    override fun close() {
        Log.i(javaClass.simpleName, "Socket $socket is now closing")
        socket?.close()
        socket = null
    }
}
