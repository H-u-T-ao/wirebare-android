package top.sankokomi.wirebare.core.util

import java.nio.ByteBuffer
import java.util.Queue

internal fun ByteBuffer.readUnsignedByte(index: Int): Int {
    return this[index].toInt() and 0x0FF
}

internal fun ByteBuffer.readUnsignedShort(index: Int): Int {
    return getShort(index).toInt() and 0x0FFFF
}

fun ByteBuffer.newString(
    position: Int = position(),
    remaining: Int = remaining()
): String {
    return String(array(), position, remaining)
}

internal fun Queue<ByteBuffer>.mergeBuffer(clear: Boolean = true): ByteBuffer {
    val pendingBuffers = this
    if (isNotEmpty()) {
        var total = 0
        for (pendingBuffer in pendingBuffers) {
            total += pendingBuffer.remaining()
        }
        var offset = 0
        val array = ByteArray(total)
        for (pendingBuffer in pendingBuffers) {
            pendingBuffer.array().copyInto(
                array,
                offset,
                pendingBuffer.position(),
                pendingBuffer.position() + pendingBuffer.remaining()
            )
            offset += pendingBuffer.remaining()
        }
        if (clear) {
            pendingBuffers.clear()
        }
        return ByteBuffer.wrap(array)
    }
    return ByteBuffer.allocate(0)
}

internal fun ByteBuffer.deepCopy(): ByteBuffer {
    return ByteBuffer.wrap(array().copyOfRange(position(), remaining()))
}