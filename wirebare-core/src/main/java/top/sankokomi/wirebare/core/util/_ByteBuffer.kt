package top.sankokomi.wirebare.core.util

import java.nio.ByteBuffer
import java.util.Queue

internal fun ByteBuffer.readUnsignedByte(index: Int): Int {
    return this[index].toInt() and 0x0FF
}

internal fun ByteBuffer.readUnsignedShort(index: Int): Int {
    return getShort(index).toInt() and 0x0FFFF
}

internal fun Queue<ByteBuffer>.mergeBuffer(): ByteBuffer {
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
                pendingBuffer.remaining() - pendingBuffer.position()
            )
            offset += pendingBuffer.remaining()
        }
        pendingBuffers.clear()
        return ByteBuffer.wrap(array)
    }
    return ByteBuffer.allocate(0)
}

internal fun ByteBuffer.clearAndPut(src: ByteBuffer) {
    clear()
    put(src)
}