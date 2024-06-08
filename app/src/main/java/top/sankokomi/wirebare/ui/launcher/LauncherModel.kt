package top.sankokomi.wirebare.ui.launcher

import android.content.Context
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.ssl.JKS
import top.sankokomi.wirebare.core.util.Level
import top.sankokomi.wirebare.ui.datastore.ProxyPolicyDataStore
import top.sankokomi.wirebare.ui.wireinfo.WireBareHttpInterceptor

object LauncherModel {

    fun startProxy(
        context: Context,
        targetPackageNameArray: Array<String>,
        onRequest: (HttpRequest) -> Unit,
        onResponse: (HttpResponse) -> Unit
    ) {
        WireBare.logLevel = Level.VERBOSE
        WireBare.startProxy {
            if (ProxyPolicyDataStore.enableSSL.value) {
                jks = JKS(
                    { context.assets.open("wirebare.jks") },
                    "wirebare",
                    "wirebare".toCharArray(),
                    "PKCS12",
                    "WB",
                    "WB"
                )
            }
            useNettyMode = true
            mtu = 10 * 1024
            tcpProxyServerCount = 5
            ipv4ProxyAddress = "10.1.10.1" to 32
            enableIpv6 = ProxyPolicyDataStore.enableIpv6.value
            ipv6ProxyAddress = "a:a:1:1:a:a:1:1" to 128
            addRoutes("0.0.0.0" to 0, "::" to 0)
            addAllowedApplications(*targetPackageNameArray)
            addAsyncHttpInterceptor(
                listOf(
                    WireBareHttpInterceptor.Factory(onRequest, onResponse)
                )
            )
        }
    }

}