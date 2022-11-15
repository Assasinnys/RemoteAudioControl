package com.dmitryzenevich.remoteaudiocontrol.data

import android.util.Log
import com.dmitryzenevich.remoteaudiocontrol.data.model.*
import com.dmitryzenevich.remoteaudiocontrol.domain.repository.SocketRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import javax.inject.Inject

class SocketRepositoryImpl @Inject constructor(
    private val socketSource: SocketSource
) : SocketRepository {

    private val moshi = Moshi.Builder().build()

    override suspend fun openSocketConnection(address: String, port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            socketSource.openSocketConnection(address, port)
            true
        } catch (e: Exception) {
            Log.e( javaClass.simpleName, "Socket connection error; address: $address, port: $port", e)
            false
        }
    }

    override suspend fun sendCommand(command: Command): Unit = withContext(Dispatchers.IO) {
        val json = when(command) {
            is VolumeIncrementCommand -> moshi.adapter(VolumeIncrementCommand::class.java).toJson(command)
            is MuteToggleCommand -> moshi.adapter(MuteToggleCommand::class.java).toJson(command)
            is SetVolumeCommand -> moshi.adapter(SetVolumeCommand::class.java).toJson(command)
        }.plus("\n")
        Log.i(javaClass.simpleName, "send: $json")

        try {
            socketSource.writer.send(json)
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "Can't send command: $command", e)
        }
    }

    override fun bindSocketInput(): Flow<Event?> {
        return socketSource.reader.lineSequence().asFlow().map {
            Log.i(javaClass.simpleName, "json: $it")
            it.toEvent(moshi)
        }
    }
}

fun OutputStreamWriter.send(str: String) {
    write(str)
    flush()
}

fun String.toEvent(moshi: Moshi): Event? {
    return when {
        contains(EventType.NEW_SESSION.event) -> moshi.adapter(NewSessionEvent::class.java).fromJson(this)
        contains(EventType.SET_NAME.event) -> moshi.adapter(SetNameEvent::class.java).fromJson(this)
        contains(EventType.MUTE_STATE_CHANGED.event) -> moshi.adapter(MuteStateChangedEvent::class.java).fromJson(this)
        contains(EventType.STATE_CHANGED.event) -> moshi.adapter(StateChangedEvent::class.java).fromJson(this)
        contains(EventType.VOLUME_CHANGED.event) -> moshi.adapter(VolumeChangedEvent::class.java).fromJson(this)
        else -> UnknownEvent
    }
}
