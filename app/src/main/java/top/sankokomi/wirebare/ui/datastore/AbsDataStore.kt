package top.sankokomi.wirebare.ui.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import top.sankokomi.wirebare.ui.util.Global

/**
 * @param VT 键值对的值类型
 * */
abstract class AbsDataStore<VT>(
    private val context: Context = Global.appContext,
    dataStoreName: String
) {

    protected abstract fun String.pref(): Preferences.Key<VT>

    private val Context.accessControlDataStore: DataStore<Preferences>
            by preferencesDataStore(dataStoreName)

    protected open suspend fun save(key: String, value: VT) {
        context.accessControlDataStore.edit {
            it[key.pref()] = value
        }
    }

    protected open suspend fun load(key: String): VT? {
        return context.accessControlDataStore
            .data.first()[key.pref()]
    }

    protected open suspend fun first(keys: List<String>): List<VT?> {
        val result = mutableListOf<VT?>()
        context.accessControlDataStore.data.first().let {
            for (key in keys) {
                result.add(it[key.pref()])
            }
        }
        return result
    }

}