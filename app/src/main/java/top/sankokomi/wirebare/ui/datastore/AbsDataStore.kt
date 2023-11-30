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
    dataStoreName: String,
    private val default: VT
) {

    protected abstract fun String.pref(): Preferences.Key<VT>

    private val Context.accessControlDataStore: DataStore<Preferences>
            by preferencesDataStore(dataStoreName)

    open suspend fun emit(keyValue: Pair<String, VT>) {
        val (key, value) = keyValue
        context.accessControlDataStore.edit {
            it[key.pref()] = value
        }
    }

    open suspend fun emitAll(keyValues: List<Pair<String, VT>>) {
        context.accessControlDataStore.edit {
            for ((key, value) in keyValues) {
                it[key.pref()] = value
            }
        }
    }

    open suspend fun collect(key: String): VT {
        return context.accessControlDataStore
            .data.first()[key.pref()] ?: default
    }

    open suspend fun collectAll(keys: List<String>): List<VT> {
        val result = mutableListOf<VT>()
        context.accessControlDataStore.data.first().let {
            for (key in keys) {
                result.add(it[key.pref()] ?: default)
            }
        }
        return result
    }

}