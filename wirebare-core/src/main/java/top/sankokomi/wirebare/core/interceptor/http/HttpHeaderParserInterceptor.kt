package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import java.nio.ByteBuffer

/**
 * Http 请求头，响应头拦截器
 * */
class HttpHeaderParserInterceptor : HttpIndexedInterceptor() {

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0 && session.request.isPlaintext == true) {
            parseHttpRequestHeader(buffer, session)
        }
        super.onRequest(chain, buffer, session, tunnel, index)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        if (index == 0 && session.response.isPlaintext == true) {
            parseHttpResponseHeader(buffer, session)
        }
        super.onResponse(chain, buffer, session, tunnel, index)
    }
}