package top.sankokomi.wirebare.core.util

import io.netty.channel.Channel
import java.net.InetSocketAddress

fun Channel.localPort(): Short {
    return (localAddress() as InetSocketAddress).port.toShort()
}

fun Channel.remotePort(): Short {
    return (remoteAddress() as InetSocketAddress).port.toShort()
}