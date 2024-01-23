package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpIndexedInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.ssl.isRequestHttps
import java.nio.ByteBuffer

class HttpSSLSniffInterceptor : HttpIndexedInterceptor() {
    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        index: Int
    ) {
        if (index == 0) {
            val (request, response) = chain.curReqRsp(session) ?: return
            val isHttps = buffer.isRequestHttps
            request.isHttps = isHttps
            response.isHttps = isHttps
            if (isHttps == null) {
                // 非 HTTP/HTTPS 或 HTTPS 但没有设置 JKS，直接 return
                return
            }
        }
        super.onRequest(chain, buffer, session, index)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        index: Int
    ) {
        val (request, _) = chain.curReqRsp(session) ?: return
        // 非 HTTP/HTTPS，直接 return
        request.isHttps ?: return
        super.onResponse(chain, buffer, session, index)
    }

}