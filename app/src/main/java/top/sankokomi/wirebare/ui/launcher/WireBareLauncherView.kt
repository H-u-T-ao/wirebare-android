package top.sankokomi.wirebare.ui.launcher

import androidx.activity.ComponentActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import top.sankokomi.wirebare.core.common.IProxyStatusListener
import top.sankokomi.wirebare.core.common.VpnProxyServiceStatus
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.HttpRequestUrlInterceptor
import top.sankokomi.wirebare.core.interceptor.Request
import top.sankokomi.wirebare.ui.util.copyTextToClipBoard
import top.sankokomi.wirebare.ui.util.showToast

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ComponentActivity.WireBareUIPage() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        var isPrepared by remember { mutableStateOf(false) }
        var inputPkg by remember { mutableStateOf("com.tencent.mm") }
        var isWireBareActive by remember { mutableStateOf(false) }
        val wireBareSwitch = remember { MutableStateFlow(false) }
        val urlList = remember { mutableStateListOf<String>() }
        WireBare.addVpnProxyStatusListener(object : IProxyStatusListener {
            override fun onVpnStatusChanged(
                oldStatus: VpnProxyServiceStatus,
                newStatus: VpnProxyServiceStatus
            ) {
                isWireBareActive = newStatus == VpnProxyServiceStatus.ACTIVE
                if (!isWireBareActive) {
                    urlList.clear()
                    wireBareSwitch.value = false
                }
            }
        })
        LaunchedEffect(Unit) {
            wireBareSwitch.collect {
                if (it && !isWireBareActive) {
                    startProxy(inputPkg) { url ->
                        urlList.add(url)
                    }
                } else if (!it && isWireBareActive) {
                    WireBare.stopProxy()
                }
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                WireBare.prepareVpnProxyService(this@WireBareUIPage) {
                    if (it) {
                        isPrepared = true
                        showToast("已授权")
                    } else {
                        isPrepared = false
                        showToast("未授权")
                    }
                }
            }
        ) {
            Text(text = "授权 VPN 服务 当前：$isPrepared")
        }
        TextField(
            value = inputPkg,
            onValueChange = {
                inputPkg = it
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        )
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            onClick = {
                if (!isPrepared) {
                    showToast("请先授权 VPN 服务")
                } else {
                    wireBareSwitch.value = !isWireBareActive
                }
            }
        ) {
            Text(text = "开始代理抓包 当前：$isWireBareActive")
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(urlList.size) {
                Text(
                    text = urlList[it],
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onLongClick = {
                                if(copyTextToClipBoard(urlList[it])) {
                                    showToast("已复制到剪贴板")
                                }
                            }
                        ) {
                            showToast("长按以复制到剪贴板")
                        }
                        .padding(8.dp)
                )
            }
        }
    }
}

private fun startProxy(
    pkg: String,
    onInterceptUrl: (String) -> Unit
) {
//         WireBare.logLevel = Level.SILENT
    WireBare.startProxy {
        mtu = 7000
        proxyAddress = "10.1.10.1" to 32
        addRoutes("0.0.0.0" to 0)
        addAllowedApplications(pkg)
        // 在这里加入拦截器，即可进行抓包
        // addRequestInterceptors(...)
        addRequestInterceptors({
            object : HttpRequestUrlInterceptor() {
                override fun onRequest(url: String) {
                    onInterceptUrl(url)
                }

                override fun onRequestFinished(request: Request) {
                }
            }
        })
    }
}