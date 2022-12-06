package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dmitryzenevich.remoteaudiocontrol.presentation.theme.Dimens.TabHeight
import com.dmitryzenevich.remoteaudiocontrol.presentation.theme.RemoteAudioControlTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dmitryzenevich.remoteaudiocontrol.R

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RemoteAudioControlApp()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteAudioControlApp(viewModel: MainViewModel = viewModel()) {
    val uiState = viewModel.uiState.collectAsState()
    val showDialog = viewModel.showAddressDialog.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()

    RemoteAudioControlTheme {
        Scaffold(
            topBar = { TopBar(viewModel::onAddressClick) }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                MainScreen(
                    uiState = uiState.value,
                    onVolumeChanged = viewModel::onVolumeChanged,
                    onMuteClick = viewModel::onMuteClick
                )
                if (showDialog.value) {
                    IpAddressDialog(initIp = viewModel.getCurrentIp(), onConfirm = viewModel::onConfirmAddress)
                }
                if (isLoading.value) {
                    ConnectionProgressBar()
                }
            }
        }
    }
}

@Composable
fun ConnectionProgressBar() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TopBar(onAddressClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .height(TabHeight)
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.app_name))
            IconButton(onClick = onAddressClick) {
                Icon(
                    imageVector = Icons.Filled.Build,
                    contentDescription = stringResource(R.string.accessibility_ip_address_button)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IpAddressDialog(initIp: String, onConfirm: (String) -> Unit) {
    var ipText by remember { mutableStateOf(initIp) }

    AlertDialog(
        onDismissRequest = {},
        title = {
            Text(text = stringResource(R.string.ip_dialog_title))
        },
        text = {
            TextField(value = ipText, onValueChange = { ipText = it })
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(ipText) }
            ) {
                Text(text = "Ok")
            }
        }
    )
}
