package top.sankokomi.wirebare.core.interceptor.tcp

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

class TcpInterceptChain(
    private val interceptors: List<TcpInterceptor>
) {

    private var interceptorIndex = -1

    /**
     * 处理请求体
     * */
    fun processRequestNext(
        buffer: ByteBuffer,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onRequest(this, buffer, session, tunnel)
    }

    /**
     * 请求体处理完毕
     * */
    fun processRequestFinishedNext(
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onRequestFinished(this, session, tunnel)
    }

    /**
     * 处理响应体
     * */
    fun processResponseNext(
        buffer: ByteBuffer,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onResponse(this, buffer, session, tunnel)
    }

    /**
     * 响应体处理完毕
     * */
    fun processResponseFinishedNext(
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex++
        interceptors.getOrNull(
            interceptorIndex
        )?.onResponseFinished(this, session, tunnel)
    }

    internal fun processRequestFirst(
        buffer: ByteBuffer,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex = -1
        processRequestNext(buffer, session, tunnel)
    }

    internal fun processRequestFinishedFirst(
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex = -1
        processRequestFinishedNext(session, tunnel)
    }

    internal fun processResponseFirst(
        buffer: ByteBuffer,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex = -1
        processResponseNext(buffer, session, tunnel)
    }

    internal fun processResponseFinishedFirst(
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        interceptorIndex = -1
        processResponseFinishedNext(session, tunnel)
    }
}