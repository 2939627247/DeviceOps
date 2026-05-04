package com.android.deviceops.viewmodel

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.deviceops.DeviceAdminReceiver
import com.android.deviceops.data.AppFilter
import com.android.deviceops.data.AppInfo
import com.android.deviceops.data.PreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageAppsViewModel : ViewModel() {

    private val _allApps = MutableStateFlow<List<AppInfo>>(emptyList())

    private val _filter = MutableStateFlow(AppFilter.ALL)
    val filter: StateFlow<AppFilter> = _filter

    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val filteredApps: StateFlow<List<AppInfo>> = _filteredApps

    private val _disabledCount = MutableStateFlow(0)
    val disabledCount: StateFlow<Int> = _disabledCount

    /** packageName → remaining seconds for test-disable countdown. */
    private val _countdowns = MutableStateFlow<Map<String, Long>>(emptyMap())

    private val countdownJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            combine(_allApps, _filter, _countdowns) { apps, filter, countdowns ->
                val withCountdown = apps.map { app ->
                    app.copy(countdownSeconds = countdowns[app.packageName])
                }
                withCountdown
                    .filter { app ->
                        when (filter) {
                            AppFilter.ALL    -> true
                            AppFilter.USER   -> !app.isSystemApp
                            AppFilter.SYSTEM -> app.isSystemApp
                        }
                    }
                    // Sort: test-disabled first → disabled second → alphabetical
                    .sortedWith(
                        compareByDescending<AppInfo> { it.countdownSeconds != null }
                            .thenByDescending { it.isDisabled }
                            .thenBy { it.label.lowercase() }
                    )
            }.collect { sorted ->
                _filteredApps.value  = sorted
                _disabledCount.value = sorted.count { it.isDisabled || it.countdownSeconds != null }
            }
        }
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    fun loadApps(context: Context) {
        if (_allApps.value.isNotEmpty()) return  // already loaded
        viewModelScope.launch(Dispatchers.IO) {
            val pm    = context.packageManager
            val dpm   = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(context, DeviceAdminReceiver::class.java)
            val isOwner = dpm.isDeviceOwnerApp(context.packageName)

            val infos = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val apps  = infos.mapNotNull { info ->
                if (info.packageName == context.packageName) return@mapNotNull null
                val isHidden = if (isOwner)
                    runCatching { dpm.isApplicationHidden(admin, info.packageName) }.getOrDefault(false)
                else false
                AppInfo(
                    packageName = info.packageName,
                    label       = pm.getApplicationLabel(info).toString(),
                    icon        = pm.getApplicationIcon(info),
                    isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isDisabled  = isHidden
                )
            }
            withContext(Dispatchers.Main) { _allApps.value = apps }
        }
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    fun getAppInfo(packageName: String): AppInfo? =
        _allApps.value.find { it.packageName == packageName }

    fun setFilter(f: AppFilter) { _filter.value = f }

    // ── Actions ───────────────────────────────────────────────────────────────

    fun enableApp(context: Context, packageName: String) {
        cancelCountdown(packageName)
        setHidden(context, packageName, false)
        updateDisabledState(packageName, false)
        val prefs = PreferencesManager(context)
        prefs.saveDisabledApps(prefs.getDisabledApps() - packageName)
    }

    fun disableApp(context: Context, packageName: String) {
        cancelCountdown(packageName)
        setHidden(context, packageName, true)
        updateDisabledState(packageName, true)
        val prefs = PreferencesManager(context)
        prefs.saveDisabledApps(prefs.getDisabledApps() + packageName)
    }

    /**
     * Test-disable: hides the app for exactly 6 minutes, then auto-enables it.
     * The countdown ticks every second and is reflected in [filteredApps].
     */
    fun testDisableApp(context: Context, packageName: String) {
        cancelCountdown(packageName)
        setHidden(context, packageName, true)
        updateDisabledState(packageName, true)

        countdownJobs[packageName] = viewModelScope.launch {
            var remaining = 6L * 60L   // 360 seconds
            while (remaining > 0) {
                _countdowns.value = _countdowns.value + (packageName to remaining)
                delay(1_000)
                remaining--
            }
            _countdowns.value = _countdowns.value - packageName
            // Auto-enable when countdown reaches zero
            enableApp(context, packageName)
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun cancelCountdown(packageName: String) {
        countdownJobs[packageName]?.cancel()
        countdownJobs.remove(packageName)
        _countdowns.value = _countdowns.value - packageName
    }

    private fun setHidden(context: Context, packageName: String, hidden: Boolean) {
        try {
            val dpm   = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin = ComponentName(context, DeviceAdminReceiver::class.java)
            dpm.setApplicationHidden(admin, packageName, hidden)
        } catch (_: Exception) { }
    }

    private fun updateDisabledState(packageName: String, disabled: Boolean) {
        _allApps.value = _allApps.value.map {
            if (it.packageName == packageName) it.copy(isDisabled = disabled) else it
        }
    }
}
