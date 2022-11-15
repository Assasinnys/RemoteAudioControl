package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.dmitryzenevich.remoteaudiocontrol.presentation.theme.RemoteAudioControlTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteAudioControlTheme {
                val uiState by viewModel.uiState.collectAsState()
                LazyRow {
                    items(uiState.volumes) { itemState ->
                        VolumeItem(itemState, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun VolumeItem(volumeItemState: VolumeItemState, viewModel: MainViewModel) {
    var volume by remember { mutableStateOf(volumeItemState.volume) }
    volume = volumeItemState.volume
    val onValueChanged = { newValue: Float ->
        Log.i("debug", "new value: $newValue")
        viewModel.onVolumeChanged(volumeItemState,  newValue.toInt())
        volume = newValue.toInt()
    }

    Column(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${volumeItemState.name}\nVolume: ${volumeItemState.volume}"
        )
        VerticalSlider(
            value = /*volumeItemState.volume.toFloat()*/volume.toFloat(),
            onValueChanged = onValueChanged
        )
    }
}

@Composable
fun VerticalSlider(
    value: Float,
    onValueChanged: (Float) -> Unit
) {
    Slider(
        value = value,
        valueRange = 0f..100f,
        steps = 99,
        onValueChange = onValueChanged,
        modifier = Modifier
            .graphicsLayer {
                rotationZ = 270f
                transformOrigin = TransformOrigin(0f, 0f)
            }
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    Constraints(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxHeight,
                    )
                )
                layout(placeable.height, placeable.width) {
                    placeable.place(-placeable.width, 0)
                }
            }
            .padding(horizontal = 24.dp)
    )
}
