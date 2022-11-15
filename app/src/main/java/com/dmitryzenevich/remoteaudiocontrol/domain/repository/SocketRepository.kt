package com.dmitryzenevich.remoteaudiocontrol.domain.repository

import com.dmitryzenevich.remoteaudiocontrol.data.model.Command
import com.dmitryzenevich.remoteaudiocontrol.data.model.Event
import kotlinx.coroutines.flow.Flow


interface SocketRepository {

    suspend fun openSocketConnection(address: String, port: Int): Boolean

    suspend fun sendCommand(command: Command)

    fun bindSocketInput(): Flow<Event?>
}