package org.github.kokomi.wirebare.util

import java.math.BigInteger

internal fun ByteArray.readByte(offset: Int): Byte {
    return this[offset]
}

internal fun ByteArray.writeByte(value: Byte, offset: Int) {
    this[offset] = value
}

internal fun ByteArray.readShort(offset: Int): Short {
    return ((this[offset].toInt() and 0xFF shl 8) or
            (this[offset + 1].toInt() and 0xFF)).toShort()
}

internal fun ByteArray.writeShort(value: Short, offset: Int) {
    this[offset] = (value.toInt() shr 8).toByte()
    this[offset + 1] = value.toByte()
}

internal fun ByteArray.readInt(offset: Int): Int {
    return (this[offset].toInt() shl 24) or
            (this[offset + 1].toInt() and 0xFF shl 16) or
            (this[offset + 2].toInt() and 0xFF shl 8) or
            (this[offset + 3].toInt() and 0xFF)
}

internal fun ByteArray.writeInt(value: Int, offset: Int) {
    this[offset] = (value shr 24).toByte()
    this[offset + 1] = (value shr 16).toByte()
    this[offset + 2] = (value shr 8).toByte()
    this[offset + 3] = value.toByte()
}

internal fun ByteArray.calculateSum(offset: Int, length: Int): BigInteger {
    var start = offset
    var size = length
    var sum = BigInteger.ZERO
    while (size > 1) {
        sum += BigInteger.valueOf((readShort(start).toInt() and 0xFFFF).toLong())
        start += 2
        size -= 2
    }
    if (size > 0) {
        sum += BigInteger.valueOf((readByte(start).toInt() and 0xFF shl 8).toLong())
    }
    return sum
}