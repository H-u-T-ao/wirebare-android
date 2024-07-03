package top.sankokomi.wirebare.core.interceptor.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.toByteBuffer

class NettyHttpClientHandler(
    private val serverCtx: () -> ChannelHandlerContext?
) : ChannelDuplexHandler() {
    override fun channelRead(
        ctx: ChannelHandlerContext,
        msg: Any
    ) {
        if (msg is ByteBuf) {
            val buffer = msg.toByteBuffer()
            msg.release()
            WireBareLogger.info("代理客户端读取 ${buffer.remaining()}")
            serverCtx.invoke()?.writeAndFlush(
                NettyWirePacket(buffer, false)
            )
        }
        super.channelRead(ctx, msg)
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise?) {
        WireBareLogger.info("代理客户端写入 ${(msg as ByteBuf).readableBytes()}")
        super.write(ctx, msg, promise)
    }
}