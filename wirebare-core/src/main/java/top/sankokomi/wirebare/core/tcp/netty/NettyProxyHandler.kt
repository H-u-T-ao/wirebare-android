package top.sankokomi.wirebare.core.tcp.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageCodec
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.newString

class NettyProxyHandler : ByteToMessageCodec<String>() {
    override fun encode(ctx: ChannelHandlerContext, msg: String?, out: ByteBuf) {
        msg?.toByteArray()?.let {
            out.writeBytes(it)
        }
    }

    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val array = ByteArray(`in`.readableBytes())
        `in`.readBytes(array)
        WireBareLogger.error(array.newString())
    }
}