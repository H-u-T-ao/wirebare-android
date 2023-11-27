package top.sankokomi.wirebare.core.common

import android.net.VpnService
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

open class VpnPrepareActivity : ComponentActivity() {

    private val resultCallback =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                onPrepareSuccess()
            } else {
                onPrepareFail()
            }
        }

    open fun onPrepareSuccess() {
    }

    open fun onPrepareFail() {
    }

    /**
     * 准备 VPN 服务，若准备成功（用户授权），则回调 [onPrepareSuccess] 否则回调 [onPrepareFail]
     * */
    fun prepareProxy() {
        val intent = VpnService.prepare(this) ?: return onPrepareSuccess()
        this.resultCallback.launch(intent)
    }

}