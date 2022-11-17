package com.dmitryzenevich.remoteaudiocontrol.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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

@Composable
fun VolumeItem(
    volumeItemState: VolumeItemState,
    onValueChanged: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${volumeItemState.name}\nVolume: ${volumeItemState.volume.value}"
        )
        VerticalSlider(
            value = volumeItemState.volume.value.toFloat(),
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