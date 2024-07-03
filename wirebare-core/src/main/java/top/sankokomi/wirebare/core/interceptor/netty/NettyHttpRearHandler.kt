package top.sankokomi.wirebare.core.interceptor.netty

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import top.sankokomi.wirebare.core.interceptor.netty.ssl.NettyHttpSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.toByteBuf
import java.nio.ByteBuffer

internal class NettyHttpRearHandler(
    private val sslCodec: NettyHttpSSLCodec?
) : ChannelDuplexHandler() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg !is NettyWireContext) {
            super.channelRead(ctx, msg)
            return
        }
        val (buffer, session, client) = msg
        if (session.request.isHttps == true && session.request.isPlaintext == true) {
            val (request, _, tcpSession) = session
            val host = request.hostInternal ?: return
            sslCodec?.responseCodec?.encode(
                tcpSession,
                host,
                buffer,
                object : SSLCallback {
                    override fun encryptSuccess(target: ByteBuffer) {
                        client.writeAndFlush(target.toByteBuf())
                    }
                }
            ) ?: WireBareLogger.warn("HTTPS 请求报文被解密了但却没有编码器")
        } else {
            client.writeAndFlush(buffer.toByteBuf())
        }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise?) {
        if (msg !is NettyWireContext) {
            super.write(ctx, msg, promise)
            return
        }
        val (buffer, session, _) = msg
        if (session.response.isHttps == true && session.response.isPlaintext == true) {
            val (_, response, tcpSession) = session
            val host = response.hostInternal ?: return
            sslCodec?.requestCodec?.encode(
                tcpSession,
                host,
                buffer,
                object : SSLCallback {
                    override fun encryptSuccess(target: ByteBuffer) {
                        ctx.channel().writeAndFlush(
                            NettyWirePacket(target, true)
                        )
                    }
                }
            ) ?: WireBareLogger.warn("HTTPS 响应报文被解密了但却没有编码器")
        } else {
            ctx.channel().writeAndFlush(
                NettyWirePacket(buffer, true)
            )
        }
    }
}