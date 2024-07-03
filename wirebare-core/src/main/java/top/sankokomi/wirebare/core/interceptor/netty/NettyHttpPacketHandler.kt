package top.sankokomi.wirebare.core.interceptor.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.toByteBuf
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

class NettyHttpPacketHandler : ChannelDuplexHandler() {

    private val pendingFlushBuffers = LinkedBlockingQueue<ByteBuffer>()

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        WireBareLogger.info("代理服务器读取 ${(msg as? ByteBuf)?.readableBytes()}")
        if (msg == null) {
            WireBareLogger.error("发现一个空请求数据包")
        } else {
            super.channelRead(ctx, msg)
        }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any?, promise: ChannelPromise) {
        if (msg == null) {
            WireBareLogger.error("发现一个空响应数据包")
        } else if (msg !is NettyWirePacket) {
            WireBareLogger.error("发现一个类型不为 NettyWirePacket 的数据包")
            super.write(ctx, msg, promise)
        } else if (!msg.directFlush) {
            super.write(ctx, msg.buffer, promise)
        } else /*if (msg.directFlush)*/ {
            // 需要直接发送的数据
            WireBareLogger.info("代理服务器写入 ${msg.buffer.remaining()}")
            pendingFlushBuffers.offer(msg.buffer)
        }
    }

    override fun flush(ctx: ChannelHandlerContext) {
        pendingFlushBuffers.removeAll {
            WireBareLogger.info("代理服务器刷入 ${it.remaining()}")
            ctx.write(it.toByteBuf(), ctx.voidPromise())
            true
        }
        super.flush(ctx)
    }
}