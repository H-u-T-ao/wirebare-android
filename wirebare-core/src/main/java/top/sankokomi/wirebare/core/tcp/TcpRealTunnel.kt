package top.sankokomi.wirebare.core.tcp

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.BufferDirection
import top.sankokomi.wirebare.core.interceptor.http.HttpVirtualGateway
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.nio.SocketNioTunnel
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.closeSafely
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

/**
 * [TcpRealTunnel] 会接收来自 [TcpProxyTunnel] 的请求字节流，
 * 将请求字节流发送到远程服务器中，接收远程服务器的响应字节流，
 * 并将响应字节流转发给 [TcpProxyTunnel] 进行处理
 *
 * @see TcpProxyServer
 * */
internal class TcpRealTunnel(
    override val channel: SocketChannel,
    override val selector: Selector,
    private val session: TcpSession,
    private val configuration: WireBareConfiguration,
    private val httpVirtualGateway: HttpVirtualGateway,
    private val proxyService: WireBareProxyService
) : SocketNioTunnel() {

    private lateinit var proxyTunnel: TcpProxyTunnel

    internal fun attachProxyTunnel(proxy: TcpProxyTunnel) {
        proxyTunnel = proxy
    }

    internal fun connectRemoteServer(address: String, port: Int) {
        if (proxyService.protect(channel.socket())) {
            channel.configureBlocking(false)
            channel.register(selector, SelectionKey.OP_CONNECT, this)
            channel.connect(InetSocketAddress(address, port))
        } else {
            throw IllegalStateException("无法保护 TCP 通道的套接字")
        }
    }

    override fun onConnected() {
        if (channel.finishConnect()) {
            proxyTunnel.onConnected()
            prepareRead()
        } else {
            throw IllegalStateException("套接字连接失败")
        }
    }

    override fun onWrite(): Int {
        val length = super.onWrite()
        WireBareLogger.inetInfo(session, "代理客户端 >> 远程服务器 $length 字节")
        return length
    }

    override fun onRead() {
        if (isClosed) {
            httpVirtualGateway.onRequestFinished(session)
            return
        }
        val buffer = ByteBuffer.allocate(configuration.mtu)
        val length = read(buffer)
        if (length < 0 || proxyTunnel.isClosed) {
            closeSafely()
            httpVirtualGateway.onRequestFinished(session)
            return
        }
        WireBareLogger.inetDebug(
            session,
            "远程服务器 >> 代理客户端 $length 字节"
        )
        val bufferQueue = httpVirtualGateway.onResponse(
            buffer, session
        )
        for ((target, direction) in bufferQueue) {
            val remaining = target.remaining() - target.position()
            when (direction) {
                BufferDirection.ProxyClient -> {
                    WireBareLogger.inetDebug(
                        session,
                        "代理客户端 >> 客户端 $remaining 字节"
                    )
                    proxyTunnel.write(target)
                }

                BufferDirection.RemoteServer -> {
                    WireBareLogger.inetDebug(
                        session,
                        "代理客户端 >> 远程服务器 $remaining 字节"
                    )
                    write(target)
                }
            }
        }
    }

    override fun onException(t: Throwable) {
        closeSafely(this, proxyTunnel)
        httpVirtualGateway.onRequestFinished(session)
        httpVirtualGateway.onResponseFinished(session)
        session.markDying()
    }

}