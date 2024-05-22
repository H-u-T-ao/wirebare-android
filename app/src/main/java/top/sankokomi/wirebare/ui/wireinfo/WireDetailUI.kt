package top.sankokomi.wirebare.ui.wireinfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import top.sankokomi.wirebare.ui.resources.WirebareUITheme

class WireDetailUI : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sessionId = intent.getStringExtra("session_id") ?: ""
        val detailMode = intent.getIntExtra("detail_mode", DetailMode.DirectHtml.ordinal)
        setContent {
            WirebareUITheme(
                isShowStatusBar = true,
                isShowNavigationBar = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LoadDetail(sessionId = sessionId, mode = detailMode)
                }
            }
        }
    }

}
