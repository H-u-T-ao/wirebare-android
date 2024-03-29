package top.sankokomi.wirebare.ui.wireinfo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import top.sankokomi.wirebare.core.common.VpnPrepareActivity
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.ui.resources.WirebareUITheme

class WireInfoUI : VpnPrepareActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = intent.getSerializableExtra("request") as? HttpRequest
        val response = intent.getSerializableExtra("response") as? HttpResponse
        setContent {
            WirebareUITheme(
                isShowNavigationBar = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (request != null) {
                        WireInfoUIPage(request = request)
                    } else if(response != null) {
                        WireInfoUIPage(response = response)
                    }
                }
            }
        }
    }

}
