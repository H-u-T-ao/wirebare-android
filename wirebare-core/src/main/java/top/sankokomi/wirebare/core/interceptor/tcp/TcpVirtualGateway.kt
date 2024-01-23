package top.sankokomi.wirebare.core.interceptor.tcp

import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.VirtualGateway
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * Tcp 虚拟网关
 * */
abstract class TcpVirtualGateway : VirtualGateway<TcpSession> {

    protected abstract val interceptorChain: TcpInterceptChain<*, *>

    override fun onRequest(
        buffer: ByteBuffer,
        session: TcpSession
    ): Pair<ByteBuffer, BufferDirection>? {
        interceptorChain.processRequestFirst(buffer, session)
        return interceptorChain.processRequestResult()
    }

    override fun onRequestFinished(session: TcpSession) {
        interceptorChain.processRequestFinishedFirst(session)
    }

    override fun onResponse(
        buffer: ByteBuffer,
        session: TcpSession
    ): Pair<ByteBuffer, BufferDirection>? {
        interceptorChain.processResponseFirst(buffer, session)
        return interceptorChain.processResponseResult()
    }

    override fun onResponseFinished(session: TcpSession) {
        interceptorChain.processResponseFinishedFirst(session)
    }

}