package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.calculateSum
import top.sankokomi.wirebare.core.util.readByte
import top.sankokomi.wirebare.core.util.readInt
import top.sankokomi.wirebare.core.util.readShort
import top.sankokomi.wirebare.core.util.writeByte
import top.sankokomi.wirebare.core.util.writeInt
import top.sankokomi.wirebare.core.util.writeShort
import java.math.BigInteger
import kotlin.experimental.and

/**
 * ipv4 包头结构如下
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
 *    |                         Source Address                        | 16
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Destination Address                      | 20
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                    Options                    |    Padding    | 24
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * IHL := IP Header Length
 */
internal class Ipv4Header(
    private val packet: ByteArray,
    private val offset: Int = 0
): IIpHeader {

    companion object {
        internal const val MIN_IPV4_LENGTH = 20
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

    internal val version: Int
        get() = packet.readByte(offset + OFFSET_VERSION).toInt() ushr 4

    internal val headerLength: Int
        get() = packet.readByte(offset + OFFSET_IP_HEADER_LENGTH).toInt() and 0xF shl 2

    internal var totalLength: Int
        get() = packet.readShort(offset + OFFSET_TOTAL_LENGTH).toInt() and 0xFFFF
        set(value) = packet.writeShort(value.toShort(), offset + OFFSET_TOTAL_LENGTH)

    override val dataLength: Int
        get() = totalLength - headerLength

    internal val identification: Short get() = packet.readShort(OFFSET_IDENTIFICATION)

    internal val flags: Byte get() = packet.readByte(OFFSET_FLAGS)

    internal val mf: Boolean get() = flags and MASK_MF == MASK_MF

    internal val df: Boolean get() = flags and MASK_DF == MASK_DF

    internal val fragmentOffset: Short get() = packet.readShort(OFFSET_FRAGMENT_OFFSET) and 0x1FFF

    override var protocol: Byte
        get() = packet[offset + OFFSET_PROTOCOL]
        set(value) = packet.writeByte(value, offset + OFFSET_PROTOCOL)

    internal var sourceAddress: IpAddress
        get() = IpAddress(packet.readInt(offset + OFFSET_SOURCE_ADDRESS))
        set(value) = packet.writeInt(value.intIpv4, offset + OFFSET_SOURCE_ADDRESS)

    internal var destinationAddress: IpAddress
        get() = IpAddress(packet.readInt(offset + OFFSET_DESTINATION_ADDRESS))
        set(value) = packet.writeInt(value.intIpv4, offset + OFFSET_DESTINATION_ADDRESS)

    internal var checkSum: Short
        get() = packet.readShort(offset + OFFSET_CHECK_SUM)
        private set(value) = packet.writeShort(value, offset + OFFSET_CHECK_SUM)

    /**
     * 计算来源 ip 地址和目的 ip 地址的异或和并返回
     * */
    internal val ipv4AddressSum: BigInteger
        get() = packet.calculateSum(offset + OFFSET_SOURCE_ADDRESS, 8)

    override val addressSum: BigInteger get() = ipv4AddressSum

    /**
     * 先将 ip 头中的校验和置为 0 ，然后重新计算校验和
     * */
    internal fun notifyCheckSum() {
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