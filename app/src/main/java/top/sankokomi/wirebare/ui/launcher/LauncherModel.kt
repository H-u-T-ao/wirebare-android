package top.sankokomi.wirebare.ui.launcher

import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.request.Request
import top.sankokomi.wirebare.core.interceptor.request.RequestInterceptor
import top.sankokomi.wirebare.core.util.Level
import java.nio.ByteBuffer

object LauncherModel {

    fun startProxy(
        targetPackageNameArray: Array<String>,
        onRequest: (Request) -> Unit
    ) {
        WireBare.logLevel = Level.DEBUG
        WireBare.startProxy {
            mtu = 7000
            tcpProxyServerCount = 10
            proxyAddress = "10.1.10.1" to 32
            addRoutes("0.0.0.0" to 0)
            addAllowedApplications(*targetPackageNameArray)
            addRequestInterceptors({
                object : RequestInterceptor() {
                    override fun onRequest(request: Request, buffer: ByteBuffer) {
                        onRequest(request)
                    }

                    override fun onRequestFinished(request: Request) {
                    }
                }
            })
        }
    }

}