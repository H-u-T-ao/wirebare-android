package top.sankokomi.wirebare.core.tcp.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import kotlinx.coroutines.CoroutineScope
import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.tcp.TcpVirtualGateway
import top.sankokomi.wirebare.core.net.Port
import top.sankokomi.wirebare.core.net.TcpSessionStore
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.tcp.ITcpServer
import java.net.InetSocketAddress

internal class NettyProxyServer(
    private val sessionStore: TcpSessionStore,
    private val tcpVirtualGateway: TcpVirtualGateway,
    private val configuration: WireBareConfiguration,
    private val proxyService: WireBareProxyService
) : ITcpServer, CoroutineScope by proxyService {

    private val channelFuture: ChannelFuture

    override val port: Port

    override fun start() {
    }

    init {
        ServerBootstrap().also { server ->
            channelFuture = server.group(NioEventLoopGroup(), NioEventLoopGroup())
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<NioSocketChannel>() {
                    override fun initChannel(ch: NioSocketChannel) {
                        ch.pipeline().addLast(NettyProxyHandler())
                    }
                })
                .bind(0)
                .sync()
            port = Port(
                (channelFuture.channel().localAddress() as InetSocketAddress).port.toShort()
            )
        }
    }
}