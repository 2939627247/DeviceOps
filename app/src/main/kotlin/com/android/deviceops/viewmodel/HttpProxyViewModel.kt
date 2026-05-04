package com.android.deviceops.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.android.deviceops.data.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class HttpProxyViewModel : ViewModel() {

    private val _host = MutableStateFlow("")
    val host: StateFlow<String> = _host

    private val _port = MutableStateFlow("")
    val port: StateFlow<String> = _port

    /**
     * True once the user has clicked 确定 and the current values match what
     * was saved — causes input text to render in [InputTextSaved] colour.
     */
    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved

    /**
     * True whenever host or port differs from the last saved snapshot.
     * Controls whether the 确定 button is active (blue) or dim.
     */
    private val _hasChanges = MutableStateFlow(false)
    val hasChanges: StateFlow<Boolean> = _hasChanges

    private var savedHost = ""
    private var savedPort = ""

    fun init(context: Context) {
        val prefs  = PreferencesManager(context)
        savedHost  = prefs.getProxyHost()
        savedPort  = prefs.getProxyPort()
        _host.value    = savedHost
        _port.value    = savedPort
        _isSaved.value = savedHost.isNotBlank() || savedPort.isNotBlank()
        _hasChanges.value = false
    }

    fun setHost(v: String) {
        _host.value    = v
        _isSaved.value = false
        _hasChanges.value = v != savedHost || _port.value != savedPort
    }

    fun setPort(v: String) {
        _port.value    = v
        _isSaved.value = false
        _hasChanges.value = _host.value != savedHost || v != savedPort
    }

    /** Persist current values; called right before popping back to main. */
    fun save(context: Context) {
        PreferencesManager(context).saveProxy(_host.value, _port.value)
        savedHost      = _host.value
        savedPort      = _port.value
        _isSaved.value = true
        _hasChanges.value = false
    }
}
