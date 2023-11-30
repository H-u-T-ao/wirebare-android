package top.sankokomi.wirebare.ui.datastore

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.ui.util.Global
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class AppDataStore(
    name: String,
    @OptIn(DelicateCoroutinesApi::class) val coroutineScope: CoroutineScope = GlobalScope
) {
    private val Context.dataStore by preferencesDataStore(name)
    val dataStoreFlow get() = Global.appContext.dataStore.data
    suspend fun edit(transform: suspend (MutablePreferences) -> Unit) {
        Global.appContext.dataStore.edit(transform)
    }
}

class AppBooleanPref(
    keyName: String,
    default: Boolean = false
) : AppPreferenceProperty<Boolean>(default) {
    override val prefKey: Preferences.Key<Boolean> = booleanPreferencesKey(keyName)
    override fun getValue(
        thisRef: AppDataStore,
        property: KProperty<*>
    ): MutableStateFlow<Boolean> {
        return keyFlow(thisRef)
    }
}

class AppStringPref(
    keyName: String,
    default: String = ""
) : AppPreferenceProperty<String>(default) {
    override val prefKey: Preferences.Key<String> = stringPreferencesKey(keyName)
    override fun getValue(
        thisRef: AppDataStore,
        property: KProperty<*>
    ): MutableStateFlow<String> {
        return keyFlow(thisRef)
    }
}

abstract class AppPreferenceProperty<T>(
    private val default: T
) : ReadOnlyProperty<AppDataStore, MutableStateFlow<T>> {
    abstract val prefKey: Preferences.Key<T>
    private var _keyFlow: MutableStateFlow<T>? = null
    protected fun keyFlow(dataStore: AppDataStore): MutableStateFlow<T> {
        _keyFlow?.let { return@keyFlow _keyFlow!! }
        return synchronized(Unit) {
            _keyFlow?.let { return@keyFlow _keyFlow!! }
            _keyFlow = MutableStateFlow(default)
            dataStore.coroutineScope.launch(Dispatchers.IO) {
                dataStore.dataStoreFlow.collect {
                    _keyFlow!!.value = it[prefKey] ?: default
                }
            }
            dataStore.coroutineScope.launch(Dispatchers.IO) {
                _keyFlow!!.collect { value ->
                    dataStore.edit {
                        it[prefKey] = value
                    }
                }
            }
            _keyFlow!!
        }
    }
}