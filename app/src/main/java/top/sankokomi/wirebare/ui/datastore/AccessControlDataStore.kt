package top.sankokomi.wirebare.ui.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

@Suppress("StaticFieldLeak")
object AccessControlDataStore : AbsDataStore<Boolean>(
    dataStoreName = "access_control",
    default = false
) {
    override fun String.pref(): Preferences.Key<Boolean> {
        return booleanPreferencesKey(this)
    }
}