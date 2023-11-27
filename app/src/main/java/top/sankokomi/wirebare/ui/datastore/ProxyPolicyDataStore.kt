package top.sankokomi.wirebare.ui.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

@Suppress("StaticFieldLeak")
object ProxyPolicyDataStore : AbsSimpleDataStore<Boolean>(
    dataStoreName = "proxy_policy"
) {
    const val BAN_AUTO_FILTER = "auto_filter"

    override fun String.pref(): Preferences.Key<Boolean> {
        return booleanPreferencesKey(this)
    }
}