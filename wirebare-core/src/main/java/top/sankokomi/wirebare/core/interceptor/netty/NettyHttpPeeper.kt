package top.sankokomi.wirebare.core.interceptor.netty

import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.newString

class NettyHttpPeeper : ChannelDuplexHandler() {
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is NettyWireContext) {
            WireBareLogger.error("请求\n" + msg.buffer.newString())
        }
        super.channelRead(ctx, msg)
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise?) {
        if (msg is NettyWireContext) {
            WireBareLogger.error("响应\n" + msg.buffer.newString())
        }
        super.write(ctx, msg, promise)
    }
}