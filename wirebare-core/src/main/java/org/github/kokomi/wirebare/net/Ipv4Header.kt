package org.github.kokomi.wirebare.net

import org.github.kokomi.wirebare.util.*
import java.math.BigInteger
import kotlin.experimental.and

/**
 * ip 包头结构如下
 *
 *    0               1               2               3
 *    0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |Version|  IHL  |Type of Service|          Total Length         |  4
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |         Identification        |Flags|      Fragment Offset    |  8
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |  Time to Live |    Protocol   |         Header Checksum       | 12
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                       Source Address                          | 16
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                    Destination Address                        | 20
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                    Options                    |    Padding    | 24
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * IHL := IP Header Length
 */
class Ipv4Header(
    private val packet: ByteArray,
    private val offset: Int
) {

    companion object {
        const val MIN_HEADER_LENGTH = 20
        private const val OFFSET_VERSION = 0
        private const val OFFSET_IP_HEADER_LENGTH = 0
        private const val OFFSET_TOTAL_LENGTH = 2
        private const val OFFSET_IDENTIFICATION = 4
        private const val OFFSET_FLAGS = 6
        private const val OFFSET_FRAGMENT_OFFSET = 6
        private const val OFFSET_PROTOCOL = 9
        private const val OFFSET_CHECK_SUM = 10
        private const val OFFSET_SOURCE_ADDRESS = 12
        private const val OFFSET_DESTINATION_ADDRESS = 16
        private const val MASK_MF = 0b00100000.toByte()
        private const val MASK_DF = 0b01000000.toByte()
    }

    val version: Int
        get() = packet.readByte(offset + OFFSET_VERSION).toInt() ushr 4

    val isIpv4: Boolean get() = version == 0b0100

    val isIpv6: Boolean get() = version == 0b0110

    val headerLength: Int
        get() = packet.readByte(offset + OFFSET_IP_HEADER_LENGTH).toInt() and 0xF shl 2

    var totalLength: Int
        get() = packet.readShort(offset + OFFSET_TOTAL_LENGTH).toInt() and 0xFFFF
        set(value) = packet.writeShort(value.toShort(), offset + OFFSET_TOTAL_LENGTH)

    val identification: Short get() = packet.readShort(OFFSET_IDENTIFICATION)

    val flags: Byte get() = packet.readByte(OFFSET_FLAGS)

    val mf: Boolean get() = flags and MASK_MF == MASK_MF

    val df: Boolean get() = flags and MASK_DF == MASK_DF

    val fragmentOffset: Short get() = packet.readShort(OFFSET_FRAGMENT_OFFSET) and 0x1FFF

    var protocol: Byte
        get() = packet[offset + OFFSET_PROTOCOL]
        set(value) = packet.writeByte(value, offset + OFFSET_PROTOCOL)

    var sourceAddress: Ipv4Address
        get() = Ipv4Address(packet.readInt(offset + OFFSET_SOURCE_ADDRESS))
        set(value) = packet.writeInt(value.int, offset + OFFSET_SOURCE_ADDRESS)

    var destinationAddress: Ipv4Address
        get() = Ipv4Address(packet.readInt(offset + OFFSET_DESTINATION_ADDRESS))
        set(value) = packet.writeInt(value.int, offset + OFFSET_DESTINATION_ADDRESS)

    var checkSum: Short
        get() = packet.readShort(offset + OFFSET_CHECK_SUM)
        private set(value) = packet.writeShort(value, offset + OFFSET_CHECK_SUM)

    val ipv4AddressSum: BigInteger
        get() = packet.calculateSum(offset + OFFSET_SOURCE_ADDRESS, 8)

    fun notifyCheckSum() {
        checkSum = 0.toShort()
        checkSum = calculateChecksum()
    }

    private fun calculateChecksum(): Short {
        var sum = packet.calculateSum(offset, headerLength)
        var next = sum shr 16
        while (next != BigInteger.ZERO) {
            sum = (sum and BigInteger.valueOf(0xFFFF)) + next
            next = sum shr 16
        }
        return sum.inv().toShort()
    }

}