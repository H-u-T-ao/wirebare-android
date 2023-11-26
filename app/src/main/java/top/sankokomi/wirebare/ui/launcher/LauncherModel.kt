package top.sankokomi.wirebare.ui.launcher

import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.HttpRequestUrlInterceptor
import top.sankokomi.wirebare.core.interceptor.Request
import top.sankokomi.wirebare.core.util.Level

object LauncherModel {

    fun startProxy(
        vararg pkg: String,
        onInterceptUrl: (String) -> Unit = {}
    ) {
        WireBare.logLevel = Level.DEBUG
        WireBare.startProxy {
            mtu = 7000
            proxyAddress = "10.1.10.1" to 32
            addRoutes("0.0.0.0" to 0)
            addAllowedApplications(*pkg)
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

}