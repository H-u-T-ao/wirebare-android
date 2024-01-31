package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

class HttpSSLRefluxInterceptor(
    private val receiver: HttpSSLRefluxReceiver
): HttpInterceptor {

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        if (chain.curReqRsp(session)?.first?.isHttps == true) {
            receiver.onRequestReflux(chain, buffer, session)
        }
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        if (chain.curReqRsp(session)?.first?.isHttps == true) {
            receiver.onResponseReflux(chain, buffer, session)
        }
    }

}