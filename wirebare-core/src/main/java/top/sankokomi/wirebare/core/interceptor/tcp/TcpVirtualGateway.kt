package top.sankokomi.wirebare.core.interceptor.tcp

import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.VirtualGateway
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer
import java.util.Queue

/**
 * Tcp 虚拟网关
 * */
abstract class TcpVirtualGateway : VirtualGateway<TcpSession> {

    protected abstract val interceptorChain: TcpInterceptChain<*, *>

    override fun onRequest(
        buffer: ByteBuffer,
        session: TcpSession
    ): Queue<Pair<ByteBuffer, BufferDirection>> {
        interceptorChain.processRequestFirst(buffer, session)
        return interceptorChain.processResults()
    }

    override fun onRequestFinished(session: TcpSession) {
        interceptorChain.processRequestFinishedFirst(session)
    }

    override fun onResponse(
        buffer: ByteBuffer,
        session: TcpSession
    ): Queue<Pair<ByteBuffer, BufferDirection>> {
        interceptorChain.processResponseFirst(buffer, session)
        return interceptorChain.processResults()
    }

    override fun onResponseFinished(session: TcpSession) {
        interceptorChain.processResponseFinishedFirst(session)
    }

}