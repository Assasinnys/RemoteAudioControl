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
import kotlin.math.absoluteValue

@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketRepositoryImpl: SocketRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    private var commandJob: Job? = null

    init {
        viewModelScope.launch {
            val isReady = socketRepositoryImpl.openSocketConnection("10.0.2.2", 54683)
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
        val volumes = _uiState.value.volumes.toMutableList()

        when(event) {
            is NewSessionEvent -> {
//                if (_uiState.value.volumes.find { it.pid == event.PID } == null) {
//                    _uiState.value = MainScreenUiState(volumes = _uiState.value.volumes + event.toVolumeItemState())
//                }
            }
            is MuteStateChangedEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(isMuted = event.isMuted)
                    volumes.set(
                        index = volumes.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
                _uiState.value = _uiState.value.copy(volumes = volumes)
            }
            is SetNameEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(name = event.name)
                    volumes.set(
                        index = volumes.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
                _uiState.value = _uiState.value.copy(volumes = volumes)
            }
            is StateChangedEvent -> {
                volumes.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(isActive = event.isActive)
                    volumes.set(
                        index = volumes.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
                _uiState.value = _uiState.value.copy(volumes = volumes)
            }
            is VolumeChangedEvent -> {
                addIfNotExist(event)
                val v = _uiState.value.volumes.toMutableList()

                v.find { event.PID == it.pid }?.let { item ->
                    val newItem = item.copy(volume = event.volume)
                    v.set(
                        index = v.indexOfFirst { it.pid == newItem.pid },
                        element = newItem
                    )
                }
                _uiState.value = _uiState.value.copy(volumes = v)
            }
            UnknownEvent -> _uiState.value = _uiState.value.copy(isError = true)
        }
    }

    fun onVolumeChanged(volumeItemState: VolumeItemState, newVolume: Int) {
        Log.i(javaClass.simpleName, "old volume: ${volumeItemState.volume}, newVolume: $newVolume")
        val increment = newVolume.minus(volumeItemState.volume)
        Log.i(javaClass.simpleName, "increment: $increment")

//        val volumes = _uiState.value.volumes.toMutableList()
//        volumes.find { volumeItemState.pid == it.pid }?.let { item ->
//            val newItem = item.copy(volume = newVolume)
//            volumes.set(
//                index = volumes.indexOfFirst { it.pid == newItem.pid },
//                element = newItem
//            )
//        }
//        _uiState.value = _uiState.value.copy(volumes = volumes)

        sendCommand(SetVolumeCommand(volumeItemState.pid, newVolume))
    }

    private fun sendCommand(command: Command) {
        commandJob?.cancel()
        commandJob = viewModelScope.launch { socketRepositoryImpl.sendCommand(command) }
    }

    private fun addIfNotExist(event: VolumeChangedEvent) {
        if (_uiState.value.volumes.find { it.pid == event.PID } == null) {
            _uiState.value = MainScreenUiState(volumes = _uiState.value.volumes + event.toVolumeItemState())
        }
    }

}

data class MainScreenUiState(
    val volumes: List<VolumeItemState> = emptyList(),
    val isError: Boolean = false
)

data class VolumeItemState(
    val pid: Long,
    val name: String = "",
    val volume: Int = 0,
    val isMuted: Boolean = false,
    val isActive: Boolean = false
)

fun NewSessionEvent.toVolumeItemState() = VolumeItemState(pid = PID)

fun VolumeChangedEvent.toVolumeItemState() = VolumeItemState(pid = PID, volume = volume)