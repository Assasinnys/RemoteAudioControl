package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitryzenevich.remoteaudiocontrol.data.SocketRepositoryImpl
import com.dmitryzenevich.remoteaudiocontrol.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketRepositoryImpl: SocketRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var commandJob: Job? = null

    init {
        viewModelScope.launch {
            val isReady = socketRepositoryImpl.openSocketConnection(/*"10.0.2.2"*/"192.168.100.9", 54683)
            if (isReady) {
                socketRepositoryImpl.bindSocketInput()
                    .filterNotNull()
                    .onEach {
                        Log.i(javaClass.simpleName, "received: $it")
                        // TODO: test it
                        withContext(Dispatchers.Main) { proceedEvent(it) }
                    }
                    .catch { Log.e(javaClass.simpleName, "Fetch event error", it) }
                    .launchIn(CoroutineScope(Dispatchers.IO))
            }
        }
    }

    private fun proceedEvent(event: Event) {
        val volumes = _uiState.value.volumes

        when(event) {
            is NewSessionEvent -> addIfNotExist(event)
            is MuteStateChangedEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(isMuted = event.isMuted)
                   volumes.set(
                        index = volumes.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
            }
            is SetNameEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(name = event.name)
                    volumes.set(
                        index = volumes.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
            }
            is StateChangedEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(isActive = event.isActive)
                    volumes.set(
                        index = volumes.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
            }
            is VolumeChangedEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
//                    val newItem = item.copy(volume = event.volume)
//                    volumes.set(
//                        index = volumes.indexOfFirst { it.pid == newItem.pid },
//                        element = newItem
//                    )
                    item.volume.value = event.volume
                }
            }
            UnknownEvent -> _uiState.value.isError // TODO: implement error state
        }
    }

    fun onVolumeChanged(volumeItemState: VolumeItemState, newVolume: Int) {
        Log.i(javaClass.simpleName, "old volume: ${volumeItemState.volume}, newVolume: $newVolume")
        volumeItemState.volume.value = newVolume
        sendCommand(SetVolumeCommand(volumeItemState.pid, newVolume))
    }

    private fun sendCommand(command: Command) {
        commandJob?.cancel()
        commandJob = viewModelScope.launch { socketRepositoryImpl.sendCommand(command) }
    }

    private fun addIfNotExist(event: NewSessionEvent) {
        if (_uiState.value.volumes.find { it.pid == event.PID } == null) {
            _uiState.value.volumes.add(event.toVolumeItemState())
        }
    }

}

class MainScreenUiState(
    val volumes: SnapshotStateList<VolumeItemState> = mutableStateListOf(),
    val isError: Boolean = false
)

data class VolumeItemState(
    val pid: Long,
    val name: String = "",
    val volume: MutableState<Int> = mutableStateOf(0),
    val isMuted: Boolean = false,
    val isActive: Boolean = false
)

fun NewSessionEvent.toVolumeItemState() = VolumeItemState(pid = PID)

//fun VolumeChangedEvent.toVolumeItemState() = VolumeItemState(pid = PID, volume = volume)