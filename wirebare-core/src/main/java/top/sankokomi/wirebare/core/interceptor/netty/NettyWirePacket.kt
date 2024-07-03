package top.sankokomi.wirebare.core.interceptor.netty

import java.nio.ByteBuffer

data class NettyWirePacket(
    val buffer: ByteBuffer,
    val directFlush: Boolean
)