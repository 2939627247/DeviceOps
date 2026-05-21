package com.android.deviceops.viewmodel

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
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

    private val _countdowns = MutableStateFlow<Map<String, Long>>(emptyMap())
    private val countdownJobs = mutableMapOf<String, Job>()

    // 图标 LRU 缓存（最多 100 个），避免重复 IO
    private val iconCache = LruCache<String, Drawable>(100)

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

    // ── 加载应用列表（不加载图标）────────────────────────────────────────────
    fun loadApps(context: Context) {
        if (_allApps.value.isNotEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            val pm      = context.packageManager
            val dpm     = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
            val admin   = ComponentName(context, DeviceAdminReceiver::class.java)
            val isOwner = dpm.isDeviceOwnerApp(context.packageName)

            // ① 去掉 GET_META_DATA，速度提升显著
            val infos = pm.getInstalledApplications(0)
            val apps  = infos.mapNotNull { info ->
                if (info.packageName == context.packageName) return@mapNotNull null
                val isHidden = if (isOwner)
                    runCatching { dpm.isApplicationHidden(admin, info.packageName) }
                        .getOrDefault(false)
                else false
                AppInfo(
                    packageName = info.packageName,
                    label       = pm.getApplicationLabel(info).toString(),
                    // ② 不在这里加载图标，改为 UI 层按需懒加载
                    isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    isDisabled  = isHidden
                )
            }
            withContext(Dispatchers.Main) { _allApps.value = apps }
        }
    }

    // ── 按需获取图标（带缓存，在 IO 线程调用）────────────────────────────────
    suspend fun getIcon(packageName: String, pm: PackageManager): Drawable? =
        withContext(Dispatchers.IO) {
            iconCache[packageName] ?: try {
                pm.getApplicationIcon(packageName).also { iconCache.put(packageName, it) }
            } catch (_: Exception) { null }
        }

    fun getAppInfo(packageName: String): AppInfo? =
        _allApps.value.find { it.packageName == packageName }

    fun setFilter(f: AppFilter) { _filter.value = f }

    // ── Actions ───────────────────────────────────────────────────────────────
    fun enableApp(context: Context, packageName: String) {
        cancelCountdown(packageName)
        setHidden(context, packageName, false)
        updateDisabledState(packageName, false)
        PreferencesManager(context).let { it.saveDisabledApps(it.getDisabledApps() - packageName) }
    }

    fun disableApp(context: Context, packageName: String) {
        cancelCountdown(packageName)
        setHidden(context, packageName, true)
        updateDisabledState(packageName, true)
        PreferencesManager(context).let { it.saveDisabledApps(it.getDisabledApps() + packageName) }
    }

    fun testDisableApp(context: Context, packageName: String) {
        cancelCountdown(packageName)
        setHidden(context, packageName, true)
        updateDisabledState(packageName, true)
        countdownJobs[packageName] = viewModelScope.launch {
            var remaining = 6L * 60L
            while (remaining > 0) {
                _countdowns.value = _countdowns.value + (packageName to remaining)
                delay(1_000)
                remaining--
            }
            _countdowns.value = _countdowns.value - packageName
            enableApp(context, packageName)
        }
    }

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
