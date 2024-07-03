package top.sankokomi.wirebare.core.util

import io.netty.buffer.ByteBuf
import java.nio.ByteBuffer

fun ByteBuf.newString(
    start: Int = readerIndex(),
    length: Int = readableBytes()
): String {
    return String(array(), start, length)
}

fun ByteBuf.toByteBuffer(): ByteBuffer {
    return ByteBuffer.allocate(readableBytes()).also {
        readBytes(it.array(), arrayOffset() + it.position(), readableBytes())
        it.position(it.position() + readableBytes())
    }
}