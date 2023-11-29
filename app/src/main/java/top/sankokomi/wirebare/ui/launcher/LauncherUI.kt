package top.sankokomi.wirebare.ui.launcher

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.common.IProxyStatusListener
import top.sankokomi.wirebare.core.common.ProxyStatus
import top.sankokomi.wirebare.core.common.VpnPrepareActivity
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.core.interceptor.response.Response
import top.sankokomi.wirebare.ui.datastore.AppProxyDataStore
import top.sankokomi.wirebare.ui.resources.WirebareUITheme
import top.sankokomi.wirebare.ui.util.requireAppDataList

class LauncherUI : VpnPrepareActivity() {

    private val _proxyStatusFlow = MutableStateFlow(ProxyStatus.DEAD)

    private val _requestFlow = MutableSharedFlow<Request>()

    private val _responseFlow = MutableSharedFlow<Response>()

    val proxyStatusFlow = _proxyStatusFlow.asStateFlow()

    val requestFlow = _requestFlow.asSharedFlow()

    val responseFlow = _responseFlow.asSharedFlow()

    fun startProxy() {
        prepareProxy()
    }

    fun stopProxy() {
        WireBare.stopProxy()
    }

    override fun onPrepareSuccess() {
        lifecycleScope.launch(Dispatchers.IO) {
            // 提前设定状态为正在启动
            _proxyStatusFlow.value = ProxyStatus.STARTING
            val proxyAppList = AppProxyDataStore.first(
                requireAppDataList().map { app -> app.packageName },
                false
            ).filter { data ->
                data.access
            }.map { data ->
                data.packageName
            }
            withContext(Dispatchers.Main) {
                LauncherModel.startProxy(
                    proxyAppList.toTypedArray(),
                    onRequest = {
                        lifecycleScope.launch {
                            _requestFlow.emit(it)
                        }
                    },
                    onResponse = {
                        lifecycleScope.launch {
                            _responseFlow.emit(it)
                        }
                    }
                )
            }
        }
    }

    private val wireBareStatusListener = object : IProxyStatusListener {
        override fun onVpnStatusChanged(oldStatus: ProxyStatus, newStatus: ProxyStatus) {
            _proxyStatusFlow.value = newStatus
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 添加 WireBare 状态监听器
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
        // 解除监听，防止内存泄露
        WireBare.removeVpnProxyStatusListener(wireBareStatusListener)
        super.onDestroy()
    }
}
