package com.dmitryzenevich.remoteaudiocontrol.presentation.helper

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrefHelper @Inject constructor(private val sPref: SharedPreferences) {

    fun getIpAddress(): String {
        return sPref.getString(KEY_IP, "192.168.100.9")!!
    }

    fun setIpAddress(ip: String) = sPref.edit { putString(KEY_IP, ip) }

    private companion object {
        const val KEY_IP = "key_ip"
    }
}