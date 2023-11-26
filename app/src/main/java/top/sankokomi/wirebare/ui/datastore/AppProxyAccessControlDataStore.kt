package top.sankokomi.wirebare.ui.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.ui.util.Global

object AppProxyAccessControlDataStore {

    private val Context.accessControlDataStore: DataStore<Preferences>
            by preferencesDataStore("access_control")

    suspend fun save(data: AppProxyAccessControl) {
        withContext(Dispatchers.IO) {
            Global.appContext.accessControlDataStore.edit {
                it[booleanPreferencesKey(data.packageName)] = data.access
            }
        }
    }

    suspend fun load(
        packageName: String,
        default: Boolean
    ): AppProxyAccessControl {
        return withContext(Dispatchers.IO) {
            AppProxyAccessControl(
                packageName,
                Global.appContext.accessControlDataStore
                    .data.first()[booleanPreferencesKey(packageName)] ?: default
            )
        }
    }

    fun collect(
        packageNameSet: Set<String>,
        default: Boolean
    ): List<AppProxyAccessControl> {
        val result = mutableListOf<AppProxyAccessControl>()
        runBlocking {
            Global.appContext.accessControlDataStore.data.first().let {
                for (packageName in packageNameSet) {
                    result.add(
                        AppProxyAccessControl(
                            packageName,
                            it[booleanPreferencesKey(packageName)] ?: default
                        )
                    )
                }
            }
        }
        return result
    }

}