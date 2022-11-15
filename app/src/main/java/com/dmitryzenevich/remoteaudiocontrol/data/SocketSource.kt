package com.dmitryzenevich.remoteaudiocontrol.data

import java.io.Closeable
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketSource @Inject constructor() : Closeable {

    private var socket: Socket? = null

    val writer
        get() = socket().getOutputStream().writer()

    val reader
        get() = socket().getInputStream().bufferedReader()

    private fun socket(): Socket = socket ?: throw IllegalStateException("Socket is not created")

    fun openSocketConnection(address: String, port: Int) {
        socket = Socket(address, port)
    }


    override fun close() {
        socket?.close()
    }
}
