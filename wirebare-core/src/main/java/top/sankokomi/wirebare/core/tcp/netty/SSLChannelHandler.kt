package top.sankokomi.wirebare.core.tcp.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandler
import io.netty.channel.ChannelPromise
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.ssl.SslContextBuilder
import java.net.SocketAddress
import javax.net.ssl.KeyManagerFactory

class SSLChannelHandler : ByteToMessageDecoder(), ChannelOutboundHandler {

    init {
//        val builder = SslContextBuilder.forServer(null as KeyManagerFactory)
//        val context = builder.build()
//        context.newHandler(null)
    }

    override fun decode(ctx: ChannelHandlerContext?, `in`: ByteBuf?, out: MutableList<Any>?) {
    }

    override fun bind(
        ctx: ChannelHandlerContext,
        localAddress: SocketAddress?,
        promise: ChannelPromise?
    ) {
        ctx.bind(localAddress, promise)
    }

    override fun connect(
        ctx: ChannelHandlerContext, remoteAddress: SocketAddress?, localAddress: SocketAddress?,
        promise: ChannelPromise?
    ) {
        ctx.connect(remoteAddress, localAddress, promise)
    }

    override fun deregister(ctx: ChannelHandlerContext, promise: ChannelPromise?) {
        ctx.deregister(promise)
    }

    override fun disconnect(
        ctx: ChannelHandlerContext?,
        promise: ChannelPromise?
    ) {
    }

    override fun close(
        ctx: ChannelHandlerContext?,
        promise: ChannelPromise?
    ) {
    }

    override fun read(ctx: ChannelHandlerContext) {
        ctx.read()
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise?) {
    }

    override fun flush(ctx: ChannelHandlerContext?) {
    }

}