package com.android.deviceops.viewmodel

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.net.ProxyInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.deviceops.DeviceAdminReceiver
import com.android.deviceops.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _proxyEnabled      = MutableStateFlow(false)
    val proxyEnabled: StateFlow<Boolean> = _proxyEnabled

    private val _manageAppsEnabled = MutableStateFlow(false)
    val manageAppsEnabled: StateFlow<Boolean> = _manageAppsEnabled

    /** Call once from the first composition. */
    fun init(context: Context) {
        val prefs = PreferencesManager(context)
        _proxyEnabled.value      = prefs.isProxyEnabled()
        _manageAppsEnabled.value = prefs.isManageAppsEnabled()
    }

    // ── HTTP Proxy toggle ─────────────────────────────────────────────────

    fun toggleProxy(context: Context) {
        val prefs    = PreferencesManager(context)
        val newState = !_proxyEnabled.value
        _proxyEnabled.value = newState
        prefs.setProxyEnabled(newState)
        viewModelScope.launch(Dispatchers.IO) {
            applyProxy(context, newState, prefs.getProxyHost(), prefs.getProxyPort())
        }
    }

    private fun applyProxy(context: Context, enable: Boolean, host: String, port: String) {
        try {
            val dpm   = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(context, DeviceAdminReceiver::class.java)
            val proxy = if (enable && host.isNotBlank() && port.isNotBlank())
                ProxyInfo.buildDirectProxy(host, port.toIntOrNull() ?: 0)
            else null
            dpm.setRecommendedGlobalProxy(admin, proxy)
        } catch (_: Exception) { /* not device owner or missing perm */ }
    }

    // ── Manage-apps toggle ────────────────────────────────────────────────

    fun toggleManageApps(context: Context) {
        val prefs    = PreferencesManager(context)
        val newState = !_manageAppsEnabled.value
        _manageAppsEnabled.value = newState
        prefs.setManageAppsEnabled(newState)
        viewModelScope.launch(Dispatchers.IO) {
            applyAppManagement(context, newState, prefs.getDisabledApps())
        }
    }

    private fun applyAppManagement(context: Context, enable: Boolean, packages: Set<String>) {
        try {
            val dpm   = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(context, DeviceAdminReceiver::class.java)
            for (pkg in packages) {
                dpm.setApplicationHidden(admin, pkg, enable)
            }
        } catch (_: Exception) { }
    }
}
