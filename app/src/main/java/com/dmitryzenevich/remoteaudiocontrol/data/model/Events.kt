package com.dmitryzenevich.remoteaudiocontrol.data.model

import com.squareup.moshi.*

sealed interface Event

@JsonClass(generateAdapter = true)
data class NewSessionEvent(
    val PID: Long
) : Event

@JsonClass(generateAdapter = true)
data class StateChangedEvent(
    val PID: Long,
    @field:Json(name = "is_active")
    val isActive: Boolean
) : Event

@JsonClass(generateAdapter = true)
data class VolumeChangedEvent(
    @field:Json(name = "new_volume")
    val volume: Int,
    val PID: Long
) : Event

@JsonClass(generateAdapter = true)
data class MuteStateChangedEvent(
    @field:Json(name = "is_muted")
    val isMuted: Boolean,
    val PID: Long
) : Event

@JsonClass(generateAdapter = true)
data class SetNameEvent(
    val name: String,
    val PID: Long
) : Event

object UnknownEvent : Event

enum class EventType(val event: String) {
    NEW_SESSION("NewSession"),
    STATE_CHANGED("StateChanged"),
    VOLUME_CHANGED("VolumeChanged"),
    MUTE_STATE_CHANGED("MuteStateChanged"),
    SET_NAME("SetName"),
    SET_VOLUME("SetVolume");
}