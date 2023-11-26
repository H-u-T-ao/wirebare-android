package top.sankokomi.wirebare.core.common

import android.net.VpnService
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

abstract class VpnPrepareActivity : ComponentActivity() {

    private val resultCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            onResult(it.resultCode == RESULT_OK)
        }

    abstract val onResult: (Boolean) -> Unit

    fun prepareProxy() {
        val intent = VpnService.prepare(this) ?: return onResult(true)
        this.resultCallback.launch(intent)
    }

}