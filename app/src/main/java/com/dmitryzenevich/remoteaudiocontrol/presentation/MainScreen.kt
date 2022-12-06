package com.dmitryzenevich.remoteaudiocontrol.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(
    uiState: MainScreenUiState,
    onVolumeChanged: (VolumeItemState, Int) -> Unit,
    onMuteClick: (VolumeItemState) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            items = uiState.volumes,
            key = { item -> item.pid }
        ) { itemState ->
            VolumeItem(
                volumeItemState = itemState,
                onValueChanged = { newValue: Float ->
                    onVolumeChanged(itemState, newValue.toInt())
                },
                onMuteClick = { onMuteClick(itemState) }
            )
        }
    }
}