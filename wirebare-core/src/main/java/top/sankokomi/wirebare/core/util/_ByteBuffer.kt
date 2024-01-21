package top.sankokomi.wirebare.core.util

import java.nio.ByteBuffer

internal fun ByteBuffer.readUnsignedByte(index: Int): Int {
    return this[index].toInt() and 0x0FF
}

internal fun ByteBuffer.readUnsignedShort(index: Int): Int {
    return getShort(index).toInt() and 0x0FFFF
}