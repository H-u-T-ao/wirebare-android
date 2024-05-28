package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.tcp.TcpInterceptChain
import top.sankokomi.wirebare.core.interceptor.tcp.TcpInterceptor
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class HttpTcpInterceptor(
    configuration: WireBareConfiguration
) : TcpInterceptor {
    private val httpVirtualGateway = HttpVirtualGateway(configuration)
    private val sessionMap = ConcurrentHashMap<TcpSession, HttpSession>()
    override fun onRequest(
        chain: TcpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        httpVirtualGateway.onRequest(buffer, takeHttpSession(session), tunnel)
        super.onRequest(chain, buffer, session, tunnel)
    }

    override fun onRequestFinished(
        chain: TcpInterceptChain,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        httpVirtualGateway.onRequestFinished(takeHttpSession(session), tunnel)
        super.onRequestFinished(chain, session, tunnel)
    }

    override fun onResponse(
        chain: TcpInterceptChain,
        buffer: ByteBuffer,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        httpVirtualGateway.onResponse(buffer, takeHttpSession(session), tunnel)
        super.onResponse(chain, buffer, session, tunnel)
    }

    override fun onResponseFinished(
        chain: TcpInterceptChain,
        session: TcpSession,
        tunnel: TcpTunnel
    ) {
        val httpSession = takeHttpSession(session)
        httpVirtualGateway.onResponseFinished(httpSession, tunnel)
        super.onResponseFinished(chain, session, tunnel)
    }

    private fun takeHttpSession(tcpSession: TcpSession): HttpSession {
        return sessionMap.computeIfAbsent(tcpSession) {
            val requestTime = System.currentTimeMillis()
            val request = HttpRequest().also {
                it.requestTime = requestTime
                it.sourcePort = tcpSession.sourcePort.port
                it.destinationAddress = tcpSession.destinationAddress.stringIp
                it.destinationPort = tcpSession.destinationPort.port
            }
            val response = HttpResponse().also {
                it.requestTime = requestTime
                it.sourcePort = tcpSession.sourcePort.port
                it.destinationAddress = tcpSession.destinationAddress.stringIp
                it.destinationPort = tcpSession.destinationPort.port
            }
            HttpSession(request, response, tcpSession)
        }
    }
}