package top.sankokomi.wirebare.ui.launcher

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.common.IProxyStatusListener
import top.sankokomi.wirebare.core.common.ProxyStatus
import top.sankokomi.wirebare.core.common.VpnPrepareActivity
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.ui.datastore.AppProxyAccessControlDataStore
import top.sankokomi.wirebare.ui.resources.WirebareUITheme
import top.sankokomi.wirebare.ui.util.requireAppDataList

class LauncherUI : VpnPrepareActivity() {

    override val onResult: (Boolean) -> Unit = {
        if (it) {
            lifecycleScope.launch(Dispatchers.IO) {
                val proxyAppList = runBlocking {
                    AppProxyAccessControlDataStore.collect(
                        requireAppDataList().map { app -> app.packageName }.toSet(),
                        false
                    ).filter { data ->
                        data.access
                    }.map { data ->
                        data.packageName
                    }
                }
                withContext(Dispatchers.Main) {
                    LauncherModel.startProxy(
                        *proxyAppList.toTypedArray(),
                        onInterceptUrl = urlListCallback
                    )
                }
            }
        }
    }

    private val wireBareStatusListener = object : IProxyStatusListener {
        override fun onVpnStatusChanged(oldStatus: ProxyStatus, newStatus: ProxyStatus) {
            proxyStatusListener(oldStatus, newStatus)
        }
    }

    private var proxyStatusListener: (ProxyStatus, ProxyStatus) -> Unit = { _, _ -> }

    private var urlListCallback: (String) -> Unit = {}

    fun startProxy(
        statusListener: ((ProxyStatus, ProxyStatus) -> Unit)? = null,
        urlCallback: ((String) -> Unit)? = null
    ) {
        proxyStatusListener = statusListener ?: proxyStatusListener
        urlListCallback = urlCallback ?: urlListCallback
        prepareProxy()
    }

    fun stopProxy() {
        WireBare.stopProxy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WireBare.addVpnProxyStatusListener(wireBareStatusListener)
        setContent {
            WirebareUITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WireBareUIPage()
                }
            }
        }
    }

    override fun onDestroy() {
        WireBare.removeVpnProxyStatusListener(wireBareStatusListener)
        super.onDestroy()
    }
}
