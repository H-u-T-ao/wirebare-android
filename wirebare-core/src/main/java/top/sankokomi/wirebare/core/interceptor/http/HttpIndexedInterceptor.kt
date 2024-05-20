package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.util.UnsupportedCall
import java.nio.ByteBuffer

abstract class HttpIndexedInterceptor : HttpInterceptor {

    private val reqIndexMap = hashMapOf<HttpSession, Int>()
    private val rspIndexMap = hashMapOf<HttpSession, Int>()

    open fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        chain.processRequestNext(this, buffer, session, tunnel)
    }

    open fun onRequestFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        chain.processRequestFinishedNext(this, session, tunnel)
    }

    open fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        chain.processResponseNext(this, buffer, session, tunnel)
    }

    open fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel,
        index: Int
    ) {
        chain.processResponseFinishedNext(this, session, tunnel)
    }

    @UnsupportedCall
    final override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        reqIndexMap.compute(session) { _, value -> (value ?: -1) + 1 }
        onRequest(chain, buffer, session, tunnel, reqIndexMap[session] ?: return)
    }

    @UnsupportedCall
    final override fun onRequestFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        onRequestFinished(chain, session, tunnel, (reqIndexMap[session] ?: return) + 1)
        reqIndexMap.remove(session)
    }

    @UnsupportedCall
    final override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        rspIndexMap.compute(session) { _, value -> (value ?: -1) + 1 }
        onResponse(chain, buffer, session, tunnel, rspIndexMap[session] ?: return)
    }

    @UnsupportedCall
    final override fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        onResponseFinished(chain, session, tunnel, (rspIndexMap[session] ?: return) + 1)
        rspIndexMap.remove(session)
    }

}