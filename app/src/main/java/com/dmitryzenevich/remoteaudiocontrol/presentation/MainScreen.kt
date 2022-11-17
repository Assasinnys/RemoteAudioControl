package com.dmitryzenevich.remoteaudiocontrol.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun MainScreen(viewModel: MainViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

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
                    viewModel.onVolumeChanged(itemState, newValue.toInt())
                },
                onMuteClick = { viewModel.onMuteClick(itemState) }
            )
        }
    }
}