package com.dmitryzenevich.remoteaudiocontrol.data.model

import com.squareup.moshi.JsonClass

sealed interface Command

@JsonClass(generateAdapter = true)
data class VolumeIncrementCommand(
    val PID: Long,
    val increment: Int,
    val event: String = "VolumeIncrement"
) : Command

@JsonClass(generateAdapter = true)
data class MuteToggleCommand(
    val PID: Long,
    val event: String = "MuteToggle"
) : Command

@JsonClass(generateAdapter = true)
data class SetVolumeCommand(
    val PID: Long,
    val volume: Int,
    val event: String = "SetVolume"
) : Command
