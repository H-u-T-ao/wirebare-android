package top.sankokomi.wirebare.ui.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey

@Suppress("StaticFieldLeak")
object AppProxyDataStore : AbsDataStore<Boolean>(
    dataStoreName = "access_control"
) {
    override fun String.pref(): Preferences.Key<Boolean> {
        return booleanPreferencesKey(this)
    }

    suspend fun save(data: AppProxyData) {
        save(data.packageName, data.access)
    }

    suspend fun load(
        packageName: String,
        default: Boolean
    ): AppProxyData {
        return AppProxyData(
            packageName,
            load(packageName) ?: default
        )
    }

    suspend fun first(
        packageNameList: List<String>,
        default: Boolean
    ): List<AppProxyData> {
        return first(
            packageNameList
        ).mapIndexedNotNull { index, b ->
            AppProxyData(
                packageNameList[index],
                b ?: default
            )
        }
    }

}