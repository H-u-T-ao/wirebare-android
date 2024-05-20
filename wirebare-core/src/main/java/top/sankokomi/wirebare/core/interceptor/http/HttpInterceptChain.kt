package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import java.nio.ByteBuffer

class HttpInterceptChain(
    private val interceptors: List<HttpInterceptor>
) {

    private val interceptorIndexMap =
        hashMapOf<HttpInterceptor, HttpInterceptor?>().also { map ->
            interceptors.forEachIndexed { index, interceptor ->
                map[interceptor] = interceptors.getOrNull(index + 1)
            }
        }

    /**
     * 处理请求体
     * */
    fun processRequestNext(
        now: HttpInterceptor?,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        nextInterceptor(now)?.onRequest(this, buffer, session, tunnel)
    }

    /**
     * 请求体处理完毕
     * */
    fun processRequestFinishedNext(
        now: HttpInterceptor?,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        nextInterceptor(now)?.onRequestFinished(this, session, tunnel)
    }

    /**
     * 处理响应体
     * */
    fun processResponseNext(
        now: HttpInterceptor?,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        nextInterceptor(now)?.onResponse(this, buffer, session, tunnel)
    }

    /**
     * 响应体处理完毕
     * */
    fun processResponseFinishedNext(
        now: HttpInterceptor?,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        nextInterceptor(now)?.onResponseFinished(this, session, tunnel)
    }

    internal fun processRequestFirst(
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processRequestNext(null, buffer, session, tunnel)
    }

    internal fun processRequestFinishedFirst(
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processRequestFinishedNext(null, session, tunnel)
    }

    internal fun processResponseFirst(
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processResponseNext(null, buffer, session, tunnel)
    }

    internal fun processResponseFinishedFirst(
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processResponseFinishedNext(null, session, tunnel)
    }

    private fun nextInterceptor(now: HttpInterceptor?): HttpInterceptor? {
        now ?: return interceptors.firstOrNull()
        return interceptorIndexMap[now]
    }
}