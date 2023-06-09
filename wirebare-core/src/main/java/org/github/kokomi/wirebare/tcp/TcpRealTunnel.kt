package org.github.kokomi.wirebare.tcp

import org.github.kokomi.wirebare.common.WireBareConfiguration
import org.github.kokomi.wirebare.interceptor.VirtualGateWay
import org.github.kokomi.wirebare.net.Session
import org.github.kokomi.wirebare.nio.SocketNioTunnel
import org.github.kokomi.wirebare.service.WireBareProxyService
import org.github.kokomi.wirebare.util.WireBareLogger
import org.github.kokomi.wirebare.util.closeSafely
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

/**
 * [TcpProxyTunnel] 会接收来自 [TcpProxyTunnel] 的请求字节流，
 * 将请求字节流发送到远程服务器中，接收远程服务器的响应字节流，
 * 并将响应字节流转发给 [TcpProxyTunnel] 进行处理
 *
 * @see TcpProxyServer
 * @see TcpProxyServer
 * */
internal class TcpRealTunnel(
    override val channel: SocketChannel,
    override val selector: Selector,
    private val session: Session,
    private val configuration: WireBareConfiguration,
    private val virtualGateWay: VirtualGateWay,
    private val proxyService: WireBareProxyService
) : SocketNioTunnel() {

    private lateinit var proxyTunnel: TcpProxyTunnel

    internal fun attachProxyTunnel(proxy: TcpProxyTunnel) {
        proxyTunnel = proxy
    }

    internal fun connectRemoteServer(address: String, port: Int) {
        if (proxyService.protect(channel.socket())) {
            if (channel.isBlocking) {
                channel.configureBlocking(false)
            }
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
        WireBareLogger.inet(session, "代理客户端 >> 远程服务器 $length 字节")
        return length
    }

    override fun onRead() {
        if (isClosed) return
        val buffer = ByteBuffer.allocate(configuration.mtu)
        val length = read(buffer)
        if (length < 0 || proxyTunnel.isClosed) {
            session.active = false
            proxyTunnel.closeSafely()
            closeSafely()
            virtualGateWay.onResponseFinished()
        } else {
            WireBareLogger.inet(session, "远程服务器 >> 代理客户端 $length 字节")
            virtualGateWay.onResponse(buffer)
            proxyTunnel.write(buffer)
        }
    }

    override fun close() {
        super.close()
        WireBareLogger.inet(session, "代理客户端套接字关闭")
    }

}