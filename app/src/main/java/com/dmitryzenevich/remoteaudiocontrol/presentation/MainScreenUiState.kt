package com.dmitryzenevich.remoteaudiocontrol.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class MainScreenUiState(
    val volumes: SnapshotStateList<VolumeItemState> = mutableStateListOf(),
    val isError: Boolean = false
)

data class VolumeItemState(
    val pid: Long,
    val name: String = "",
    val volume: MutableState<Int> = mutableStateOf(0),
    val isMuted: MutableState<Boolean> = mutableStateOf(false),
    val isActive: Boolean = false
)