package top.sankokomi.wirebare.ui.launcher

import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.util.Level
import java.nio.ByteBuffer

object LauncherModel {

    fun startProxy(
        targetPackageNameArray: Array<String>,
        onRequest: (HttpRequest) -> Unit,
        onResponse: (HttpResponse) -> Unit
    ) {
        WireBare.logLevel = Level.DEBUG
        WireBare.startProxy {
            mtu = 10000
            tcpProxyServerCount = 1
            proxyAddress = "10.1.10.1" to 32
            addRoutes("0.0.0.0" to 0)
            addAllowedApplications(*targetPackageNameArray)
            setHttpInterceptorFactory {
                object : HttpIndexedInterceptor() {
                    override fun onRequest(
                        chain: HttpInterceptChain,
                        buffer: ByteBuffer,
                        session: TcpSession,
                        index: Int
                    ) {
                        if (index == 0) {
                            chain.curReqRsp(session)?.let { (req, _) ->
                                onRequest(req)
                            }
                        }
                        chain.processRequestNext(buffer, session)
                    }

                    override fun onResponse(
                        chain: HttpInterceptChain,
                        buffer: ByteBuffer,
                        session: TcpSession,
                        index: Int
                    ) {
                        if (index == 0) {
                            chain.curReqRsp(session)?.let { (_, rsp) ->
                                onResponse(rsp)
                            }
                        }
                        chain.processResponseNext(buffer, session)
                    }
                }
            }
        }
    }

}