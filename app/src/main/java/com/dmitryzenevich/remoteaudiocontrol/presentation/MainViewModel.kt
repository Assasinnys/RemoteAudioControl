package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitryzenevich.remoteaudiocontrol.data.SocketRepositoryImpl
import com.dmitryzenevich.remoteaudiocontrol.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

const val NOT_BLOCKED = -1L

@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketRepositoryImpl: SocketRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    @Volatile
    private var blockingPid: Long = NOT_BLOCKED
    private var blockingJob: Job? = null

    init {
        viewModelScope.launch {
            val isReady = socketRepositoryImpl.openSocketConnection(/*"10.0.2.2"*/"192.168.100.9", 54683)
            if (isReady) {
                socketRepositoryImpl.bindSocketInput()
                    .filterNotNull()
                    .onEach {
                        Log.i(javaClass.simpleName, "received: $it")
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
                    item.isMuted.value = event.isMuted
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
                if (blockingPid == event.PID) return

                volumes.find { event.PID == it.pid }?.let { item ->
                    item.volume.value = event.volume
                    blockVolumeEvent(event.PID)
                }
            }
            UnknownEvent -> _uiState.value.isError // TODO: implement error state
        }
    }

    private fun blockVolumeEvent(eventPid: Long) {
        blockingJob?.cancel()
        blockingJob = viewModelScope.launch {
            blockingPid = eventPid
            delay(200)
            blockingPid = NOT_BLOCKED
        }
    }

    fun onVolumeChanged(volumeItemState: VolumeItemState, newVolume: Int) {
        Log.i(javaClass.simpleName, "old volume: ${volumeItemState.volume}, newVolume: $newVolume")
        volumeItemState.volume.value = newVolume
        sendCommand(SetVolumeCommand(volumeItemState.pid, newVolume))
    }

    fun onMuteClick(volumeItemState: VolumeItemState) {
        Log.i(javaClass.simpleName, "new checked: ${!volumeItemState.isMuted.value}")
        volumeItemState.isMuted.value = !volumeItemState.isMuted.value
        sendCommand(MuteToggleCommand(volumeItemState.pid))
    }

    private fun sendCommand(command: Command) {
        viewModelScope.launch { socketRepositoryImpl.sendCommand(command) }
    }

    private fun addIfNotExist(event: NewSessionEvent) {
        if (_uiState.value.volumes.find { it.pid == event.PID } == null) {
            _uiState.value.volumes.add(event.toVolumeItemState())
        }
    }
}

fun NewSessionEvent.toVolumeItemState() = VolumeItemState(pid = PID)
