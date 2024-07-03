package top.sankokomi.wirebare.core.interceptor.netty

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.MessageToMessageCodec
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.net.Port
import top.sankokomi.wirebare.core.net.TcpSessionStore
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.remotePort
import top.sankokomi.wirebare.core.util.toByteBuffer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

internal class NettyHttpSniffConverter(
    private val sessionStore: TcpSessionStore
) : MessageToMessageCodec<ByteBuf, ByteBuffer>() {

    private var httpSession: HttpSession? = null
    private var serverCtx: ChannelHandlerContext? = null
    private var clientFuture: ChannelFuture? = null

    override fun channelActive(ctx: ChannelHandlerContext) {
        if (httpSession == null) {
            serverCtx = ctx
            generateHttpSession(ctx)
            generateClientChannel()
        }
        super.channelActive(ctx)
    }

    override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
        val session = httpSession
        if (session == null) {
            WireBareLogger.error("一个 TCP 请求因为找不到指定会话而代理失败")
            return
        }
        val future = clientFuture
        if (future == null) {
            WireBareLogger.error("代理客户端创建失败")
            return
        }
        out += NettyWireContext(msg.toByteBuffer(), session, future.channel())
        msg.release()
    }

    override fun encode(ctx: ChannelHandlerContext, msg: ByteBuffer, out: MutableList<Any>) {
        val session = httpSession
        if (session == null) {
            WireBareLogger.error("一个 TCP 请求因为找不到指定会话而代理失败")
            return
        }
        val future = clientFuture
        if (future == null) {
            WireBareLogger.error("代理客户端创建失败")
            return
        }
        out += NettyWireContext(msg, session, future.channel())
    }

    private fun generateClientChannel() {
        val tcpSession = httpSession?.tcpSession ?: return
        clientFuture = Bootstrap().group(NioEventLoopGroup())
            .channel(NioSocketChannel::class.java)
            .handler(object : ChannelInitializer<NioSocketChannel>() {
                override fun initChannel(ch: NioSocketChannel) {
                    ch.pipeline().addLast(
                        NettyHttpClientHandler(::serverCtx)
                    )
                }
            })
            .connect(
                InetSocketAddress(
                    tcpSession.destinationAddress.stringIp,
                    tcpSession.destinationPort.port.toInt()
                )
            )
            .sync()
    }

    private fun generateHttpSession(ctx: ChannelHandlerContext) {
        if (httpSession != null) return
        val tcpSession = sessionStore.query(Port(ctx.channel().remotePort())) ?: return
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
        httpSession = HttpSession(request, response, tcpSession)
    }
}