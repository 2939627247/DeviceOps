package com.android.deviceops.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── HTTP Proxy ────────────────────────────────────────────────────────────

    fun getProxyHost(): String = prefs.getString(KEY_PROXY_HOST, "") ?: ""
    fun getProxyPort(): String = prefs.getString(KEY_PROXY_PORT, "") ?: ""

    fun saveProxy(host: String, port: String) = prefs.edit {
        putString(KEY_PROXY_HOST, host)
        putString(KEY_PROXY_PORT, port)
    }

    fun isProxyEnabled(): Boolean = prefs.getBoolean(KEY_PROXY_ENABLED, false)
    fun setProxyEnabled(enabled: Boolean) = prefs.edit { putBoolean(KEY_PROXY_ENABLED, enabled) }

    // ── Managed / Disabled Apps ───────────────────────────────────────────────

    fun getDisabledApps(): Set<String> =
        prefs.getStringSet(KEY_DISABLED_APPS, emptySet()) ?: emptySet()

    fun saveDisabledApps(packages: Set<String>) = prefs.edit {
        putStringSet(KEY_DISABLED_APPS, packages)
    }

    fun isManageAppsEnabled(): Boolean = prefs.getBoolean(KEY_MANAGE_APPS_ENABLED, false)
    fun setManageAppsEnabled(enabled: Boolean) =
        prefs.edit { putBoolean(KEY_MANAGE_APPS_ENABLED, enabled) }

    companion object {
        private const val PREFS_NAME          = "deviceops_prefs"
        private const val KEY_PROXY_HOST       = "proxy_host"
        private const val KEY_PROXY_PORT       = "proxy_port"
        private const val KEY_PROXY_ENABLED    = "proxy_enabled"
        private const val KEY_DISABLED_APPS    = "disabled_apps"
        private const val KEY_MANAGE_APPS_ENABLED = "manage_apps_enabled"
    }
}
