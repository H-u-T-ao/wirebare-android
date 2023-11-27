package top.sankokomi.wirebare.ui.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import top.sankokomi.wirebare.ui.util.Global

abstract class AbsSimpleDataStore<VT>(
    private val context: Context = Global.appContext,
    dataStoreName: String
):AbsDataStore<VT>(
    context, dataStoreName
) {

    public override suspend fun save(key: String, value: VT) {
        super.save(key, value)
    }

    public override suspend fun load(key: String): VT? {
        return super.load(key)
    }

    public override suspend fun first(keys: List<String>): List<VT?> {
        return super.first(keys)
    }

}