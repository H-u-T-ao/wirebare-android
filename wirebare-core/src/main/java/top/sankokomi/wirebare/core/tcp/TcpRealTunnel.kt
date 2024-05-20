package top.sankokomi.wirebare.core.tcp

import top.sankokomi.wirebare.core.common.EventSynopsis
import top.sankokomi.wirebare.core.common.ImportantEvent
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.interceptor.tcp.TcpVirtualGateway
import top.sankokomi.wirebare.core.net.IpVersion
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.nio.SocketNioTunnel
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.closeSafely
import top.sankokomi.wirebare.core.util.ipVersion
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
    private val tcpVirtualGateway: TcpVirtualGateway,
    private val proxyService: WireBareProxyService
) : SocketNioTunnel(), TcpTunnel {

    private lateinit var proxyTunnel: TcpProxyTunnel

    internal fun attachProxyTunnel(proxy: TcpProxyTunnel) {
        proxyTunnel = proxy
    }

    private val remoteAddress = session.destinationAddress.stringIp
    private val remotePort = session.destinationPort.port.toInt() and 0xFFFF

    internal fun connectRemoteServer() {
        if (proxyService.protect(channel.socket())) {
            channel.configureBlocking(false)
            channel.register(selector, SelectionKey.OP_CONNECT, this)
            WireBareLogger.warn("开始连接远程服务器 $remoteAddress:$remotePort")
            runCatching {
                channel.connect(InetSocketAddress(remoteAddress, remotePort))
            }.onFailure {
                reportExceptionWhenConnect(remoteAddress, remotePort, it)
                WireBareLogger.error(it)
                onException(it)
            }
        } else {
            throw IllegalStateException("无法保护 TCP 通道的套接字")
        }
    }

    override fun onConnected() {
        WireBareLogger.warn("远程服务器连接完成 $remoteAddress:$remotePort")
        if (channel.finishConnect()) {
            proxyTunnel.onConnected()
            prepareRead()
        } else {
            throw IllegalStateException("套接字连接失败")
        }
    }

    override fun onWrite(): Int {
        val length = super.onWrite()
        WireBareLogger.inetDebug(session, "代理客户端 >> 远程服务器 $length 字节")
        return length
    }

    override fun onRead() {
        if (isClosed) {
            tcpVirtualGateway.onRequestFinished(session, this)
            return
        }
        val buffer = ByteBuffer.allocate(configuration.mtu)
        val length = read(buffer)
        if (length < 0 || proxyTunnel.isClosed) {
            closeSafely()
            tcpVirtualGateway.onRequestFinished(session, this)
            return
        }
        WireBareLogger.inetVerbose(
            session,
            "代理客户端 << 远程服务器 $length 字节"
        )
        tcpVirtualGateway.onResponse(buffer, session, this)
    }

    private fun reportExceptionWhenConnect(address: String, port: Int, t: Throwable?) {
        when (address.ipVersion) {
            IpVersion.IPv4 -> {
                WireBare.postImportantEvent(
                    ImportantEvent(
                        "连接远程服务器 $address:$port 时出现错误",
                        EventSynopsis.IPV4_UNREACHABLE,
                        t
                    )
                )
            }

            IpVersion.IPv6 -> {
                WireBare.postImportantEvent(
                    ImportantEvent(
                        "连接远程服务器 $address:$port 时出现错误",
                        EventSynopsis.IPV6_UNREACHABLE,
                        t
                    )
                )
            }

            else -> {}
        }
    }

    override fun onException(t: Throwable) {
        closeSafely(this, proxyTunnel)
        tcpVirtualGateway.onRequestFinished(session, this)
    }

    override fun writeToRemoteServer(buffer: ByteBuffer) {
        write(buffer)
    }

    override fun writeToLocalClient(buffer: ByteBuffer) {
        proxyTunnel.write(buffer)
    }
}