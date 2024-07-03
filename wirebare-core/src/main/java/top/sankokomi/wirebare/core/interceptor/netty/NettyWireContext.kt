package top.sankokomi.wirebare.core.interceptor.netty

import io.netty.channel.Channel
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import java.nio.ByteBuffer

/**
 * @param channelFuture 如果当前是服务端的管道，则为客户端的通道；如果当前是客户端的管道，则为服务端的通道
 * */
data class NettyWireContext(
    val buffer: ByteBuffer,
    val session: HttpSession,
    val channelFuture: Channel
)