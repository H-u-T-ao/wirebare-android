package top.sankokomi.wirebare.core.tcp

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.http.HttpVirtualGateway
import top.sankokomi.wirebare.core.net.Port
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.nio.SocketNioTunnel
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.closeSafely
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

/**
 * [TcpProxyTunnel] 会将被 [TcpProxyServer] 拦截的请求字节流转发给 [TcpRealTunnel] ，
 * [TcpRealTunnel] 请求远程服务器后会将远程服务器响应的字节流转发给 [TcpProxyTunnel] ，
 * 最后 [TcpProxyTunnel] 会将接收到的来自 [TcpRealTunnel] 的响应字节流返回给 [TcpProxyServer] ，
 * [TcpProxyServer] 将响应字节流返回给被代理的客户端，这样就完成了整个代理过程
 *
 * 在 [TcpProxyTunnel] 将请求字节流转发给 [TcpRealTunnel] 之前，
 * 会先将代理请求的字节流交由 [httpVirtualGateway] 来进行拦截，然后再进行转发
 *
 * 在 [TcpProxyTunnel] 接收到由 [TcpRealTunnel] 转发的响应字节流之后，
 * 会先将代理响应的字节流交由 [httpVirtualGateway] 来进行拦截，然后再转发给 [TcpProxyServer]
 *
 * 请求过程如下（包含拦截）：
 *
 * Real Client >> [TcpProxyServer] >> [TcpProxyTunnel] >>
 * [httpVirtualGateway] >> [TcpRealTunnel] >> Remote Server
 *
 * 响应过程如下（包含拦截）：
 *
 * Remote Server >> [TcpRealTunnel] >> [TcpProxyTunnel] >>
 * [httpVirtualGateway] >> [TcpProxyServer] >> Real Client
 *
 * @see TcpProxyServer
 * @see TcpRealTunnel
 * */
internal class TcpProxyTunnel(
    override val channel: SocketChannel,
    override val selector: Selector,
    private val port: Port,
    private val session: TcpSession,
    private val httpVirtualGateway: HttpVirtualGateway,
    private val configuration: WireBareConfiguration
) : SocketNioTunnel() {

    private lateinit var realTunnel: TcpRealTunnel

    internal fun attachRealChannel(real: TcpRealTunnel) {
        realTunnel = real
    }

    override fun onConnected() {
        prepareRead()
    }

    override fun onWrite(): Int {
        val length = super.onWrite()
        WireBareLogger.inetInfo(
            session,
            "代理服务器 $port >> 客户端 ${session.sourcePort} $length 字节"
        )
        return length
    }

    override fun onRead() {
        if (isClosed) {
            httpVirtualGateway.onResponseFinished(session)
            session.markDying()
            return
        }
        val buffer = ByteBuffer.allocate(configuration.mtu)
        val length = read(buffer)
        if (length < 0 || realTunnel.isClosed) {
            closeSafely()
            httpVirtualGateway.onResponseFinished(session)
            session.markDying()
            return
        }
        WireBareLogger.inetDebug(
            session,
            "客户端 >> 代理服务器 $port $length 字节"
        )
        val bufferQueue = httpVirtualGateway.onRequest(
            buffer, session
        )
        for ((target, direction) in bufferQueue) {
            val remaining = target.remaining() - target.position()
            when (direction) {
                BufferDirection.RemoteServer -> {
                    WireBareLogger.inetDebug(
                        session,
                        "代理服务器 $port >> 远程服务器 $remaining 字节"
                    )
                    realTunnel.write(target)
                }

                BufferDirection.ProxyClient -> {
                    WireBareLogger.inetDebug(
                        session,
                        "代理服务器 $port >> 客户端 $remaining 字节"
                    )
                    write(target)
                }
            }
        }
    }

    override fun onException(t: Throwable) {
        closeSafely(this, realTunnel)
        httpVirtualGateway.onRequestFinished(session)
        httpVirtualGateway.onResponseFinished(session)
        session.markDying()
    }

}