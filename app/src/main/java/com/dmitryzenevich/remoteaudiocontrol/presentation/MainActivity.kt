package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import com.dmitryzenevich.remoteaudiocontrol.presentation.theme.RemoteAudioControlTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteAudioControlTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    LazyRow {
        items(
            items = uiState.volumes,
            key = { item -> item.pid }
        ) { itemState ->
            VolumeItem(
                volumeItemState = itemState,
                onValueChanged = { newValue: Float ->
                    Log.i("debug", "new value: $newValue")
                    viewModel.onVolumeChanged(itemState,  newValue.toInt())
                }
            )
        }
    }
}
