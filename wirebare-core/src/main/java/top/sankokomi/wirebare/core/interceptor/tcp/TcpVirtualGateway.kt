package top.sankokomi.wirebare.core.interceptor.tcp

import top.sankokomi.wirebare.core.interceptor.VirtualGateway
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * Tcp 虚拟网关
 * */
abstract class TcpVirtualGateway : VirtualGateway<TcpSession> {

    protected abstract val interceptorChain: TcpInterceptChain<*, *>

    override fun onRequest(buffer: ByteBuffer, session: TcpSession) {
        interceptorChain.processRequest(buffer, session)
    }

    override fun onRequestFinished(session: TcpSession) {
        interceptorChain.processRequestFinished(session)
    }

    override fun onResponse(buffer: ByteBuffer, session: TcpSession) {
        interceptorChain.processResponse(buffer, session)
    }

    override fun onResponseFinished(session: TcpSession) {
        interceptorChain.processResponseFinished(session)
    }

}