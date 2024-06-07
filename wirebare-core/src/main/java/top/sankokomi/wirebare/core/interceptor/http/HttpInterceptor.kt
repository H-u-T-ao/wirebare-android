package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.http.async.AsyncHttpInterceptor
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import java.nio.ByteBuffer

/**
 * HTTP 阻塞拦截器，支持对报文进行修改，但会延长响应的耗时
 *
 * @see [AsyncHttpInterceptor]
 * */
interface HttpInterceptor {
    fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        chain.processRequestNext(this, buffer, session, tunnel)
    }

    fun onRequestFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        chain.processRequestFinishedNext(this, session, tunnel)
    }

    fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        chain.processResponseNext(this, buffer, session, tunnel)
    }

    fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        chain.processResponseFinishedNext(this, session, tunnel)
    }
}