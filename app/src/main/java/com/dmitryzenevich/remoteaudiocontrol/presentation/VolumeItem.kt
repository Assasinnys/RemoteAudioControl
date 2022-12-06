package com.dmitryzenevich.remoteaudiocontrol.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.dmitryzenevich.remoteaudiocontrol.R

@Composable
fun VolumeItem(
    volumeItemState: VolumeItemState,
    onValueChanged: (Float) -> Unit,
    onMuteClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = volumeItemState.name)
        Text(text = volumeItemState.volume.value.toString())
        VerticalSlider(
            value = volumeItemState.volume.value.toFloat(),
            onValueChanged = onValueChanged,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        IconButton(onClick = onMuteClick) {
            if (volumeItemState.isMuted.value)
                Icon(
                    painter = painterResource(R.drawable.ic_volume_off),
                    contentDescription = stringResource(R.string.accessibility_muted)
                )
            else
                Icon(
                    painter = painterResource(R.drawable.ic_volume_up),
                    contentDescription = stringResource(R.string.accessibility_resumed)
                )
        }
    }
}

@Composable
fun VerticalSlider(
    value: Float,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Slider(
        value = value,
        valueRange = 0f..100f,
        onValueChange = onValueChanged,
        modifier = modifier
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
            .padding(horizontal = 8.dp)
    )
}