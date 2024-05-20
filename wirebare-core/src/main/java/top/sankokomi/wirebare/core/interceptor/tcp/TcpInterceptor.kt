package top.sankokomi.wirebare.core.interceptor.tcp

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

interface TcpInterceptor {

    fun onRequest(chain: TcpInterceptChain, buffer: ByteBuffer, session: TcpSession, tunnel: TcpTunnel) {
        chain.processRequestNext(buffer, session, tunnel)
    }

    fun onRequestFinished(chain: TcpInterceptChain, session: TcpSession, tunnel: TcpTunnel) {
        chain.processRequestFinishedNext(session, tunnel)
    }

    fun onResponse(chain: TcpInterceptChain, buffer: ByteBuffer, session: TcpSession, tunnel: TcpTunnel) {
        chain.processResponseNext(buffer, session, tunnel)
    }

    fun onResponseFinished(chain: TcpInterceptChain, session: TcpSession, tunnel: TcpTunnel) {
        chain.processResponseFinishedNext(session, tunnel)
    }

}