package com.dmitryzenevich.remoteaudiocontrol.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dmitryzenevich.remoteaudiocontrol.presentation.theme.RemoteAudioControlTheme
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
