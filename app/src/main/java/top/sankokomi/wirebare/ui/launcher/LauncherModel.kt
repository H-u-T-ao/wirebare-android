package top.sankokomi.wirebare.ui.launcher

import android.util.Log
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.Request
import top.sankokomi.wirebare.core.interceptor.http.Response
import top.sankokomi.wirebare.core.util.Level
import java.nio.ByteBuffer

object LauncherModel {

    fun startProxy(
        targetPackageNameArray: Array<String>,
        onRequest: (Request) -> Unit,
        onResponse: (Response) -> Unit
    ) {
        WireBare.logLevel = Level.DEBUG
        WireBare.startProxy {
            mtu = 10000
            tcpProxyServerCount = 1
            proxyAddress = "10.1.10.1" to 32
            addRoutes("0.0.0.0" to 0)
            addAllowedApplications(*targetPackageNameArray)
            setHttpInterceptorFactory {
                object : HttpIndexedInterceptChain() {
                    override fun onRequest(
                        chain: HttpInterceptChain,
                        buffer: ByteBuffer,
                        index: Int
                    ) {
                        if (index == 0) {
                            Log.i("TAG", chain.request.toString())
                            onRequest(chain.request)
                        }
                        chain.processRequestNext(buffer)
                    }

                    override fun onResponse(
                        chain: HttpInterceptChain,
                        buffer: ByteBuffer,
                        index: Int
                    ) {
                        if (index == 0) {
                            Log.i("TAG", chain.response.toString())
                            onResponse(chain.response)
                        }
                        chain.processResponseNext(buffer)
                    }
                }
            }
        }
    }

}