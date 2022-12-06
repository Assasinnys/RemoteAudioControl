package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dmitryzenevich.remoteaudiocontrol.data.SocketRepositoryImpl
import com.dmitryzenevich.remoteaudiocontrol.data.model.*
import com.dmitryzenevich.remoteaudiocontrol.presentation.helper.PrefHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

const val NOT_BLOCKED = -1L

@HiltViewModel
class MainViewModel @Inject constructor(
    private val socketRepositoryImpl: SocketRepositoryImpl,
    private val prefHelper: PrefHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState = _uiState.asStateFlow()

    private val _showAddressDialog = MutableStateFlow(false)
    val showAddressDialog = _showAddressDialog.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    @Volatile
    private var blockingPid: Long = NOT_BLOCKED
    private var blockingJob: Job? = null

    init {
        connectToSocket()
    }

    private fun connectToSocket() {
        viewModelScope.launch {
            _isLoading.value = true
            val isReady = socketRepositoryImpl.openSocketConnection(prefHelper.getIpAddress(), 54683)
            if (isReady) {
                socketRepositoryImpl.bindSocketInput()
                    .filterNotNull()
                    .onEach {
                        Log.i(javaClass.simpleName, "Received: $it")
                        withContext(Dispatchers.Main) { proceedEvent(it) }
                    }
                    .catch { Log.e(javaClass.simpleName, "Fetch event error", it) }
                    .launchIn(CoroutineScope(Dispatchers.IO))
            }
            _isLoading.value = false
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

    fun onAddressClick() {
        _showAddressDialog.value = true
        Log.i(javaClass.simpleName, "address clicked")
    }

    fun onConfirmAddress(ip: String) {
        if (isValidIp(ip)) {
            prefHelper.setIpAddress(ip)
            _showAddressDialog.value = false
            connectToSocket()
            Log.i(javaClass.simpleName, "new ip: $ip")
        }
    }

    fun getCurrentIp() = prefHelper.getIpAddress().also { Log.i(javaClass.simpleName, "Current ip: $it") }

    private fun isValidIp(ip: String): Boolean = ip.matches(ipCheckRegex.toRegex())
        .also { Log.i(javaClass.simpleName, "Regex matches: $it") }
}

const val ipCheckRegex = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}\$"

fun NewSessionEvent.toVolumeItemState() = VolumeItemState(pid = PID)
