package top.sankokomi.wirebare.ui.wireinfo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import top.sankokomi.wirebare.core.common.VpnPrepareActivity
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.ui.resources.WirebareUITheme

class WireInfoUI : VpnPrepareActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = intent.getSerializableExtra("request") as Request
        setContent {
            WirebareUITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WireInfoUIPage(request = request)
                }
            }
        }
    }

}
