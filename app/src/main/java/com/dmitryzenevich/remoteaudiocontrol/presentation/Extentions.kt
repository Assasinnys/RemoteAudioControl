package com.dmitryzenevich.remoteaudiocontrol.presentation

import kotlin.math.roundToInt

fun Float.classicRound(): Float = times(100).roundToInt().div(100f)
