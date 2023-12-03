package top.sankokomi.wirebare.ui.launcher

import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.Request
import top.sankokomi.wirebare.core.interceptor.http.Response
import top.sankokomi.wirebare.core.net.Session
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
                object : HttpIndexedInterceptor() {
                    override fun onRequest(
                        chain: HttpInterceptChain,
                        buffer: ByteBuffer,
                        session: Session,
                        index: Int
                    ) {
                        if (index == 0) {
                            chain.getReqRsp(session)?.let { (req, _) ->
                                onRequest(req)
                            }
                        }
                        chain.processRequestNext(buffer, session)
                    }

                    override fun onResponse(
                        chain: HttpInterceptChain,
                        buffer: ByteBuffer,
                        session: Session,
                        index: Int
                    ) {
                        if (index == 0) {
                            chain.getReqRsp(session)?.let { (_, rsp) ->
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