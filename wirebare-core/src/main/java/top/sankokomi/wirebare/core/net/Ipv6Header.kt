package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.calculateSum
import top.sankokomi.wirebare.core.util.readByte
import top.sankokomi.wirebare.core.util.readLong
import top.sankokomi.wirebare.core.util.readShort
import top.sankokomi.wirebare.core.util.writeLong
import top.sankokomi.wirebare.core.util.writeShort
import java.math.BigInteger

/**
 * ipv6 包头结构如下
 *
 *    0               1               2               3
 *    0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |Version| Traffic class |              Flow Label               |  4
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |         Payload length        |  Next header  |   Hop limit   |  8
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                         Source Address                        | 12
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                         Source Address                        | 16
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                         Source Address                        | 20
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                         Source Address                        | 24
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Destination Address                      | 28
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Destination Address                      | 32
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Destination Address                      | 36
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                      Destination Address                      | 40
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                       Extension Headers                       | 44
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 */
internal class Ipv6Header(
    internal val packet: ByteArray,
    internal val offset: Int = 0
): IIpHeader {

    companion object {
        internal const val IPV6_STANDARD_LENGTH = 40
        private const val OFFSET_VERSION = 0
        private const val OFFSET_PAYLOAD_LENGTH = 4
        private const val OFFSET_STANDARD_NEXT_HEADER = 6
        private const val OFFSET_SOURCE_ADDRESS_FIRST_64 = 8
        private const val OFFSET_SOURCE_ADDRESS_LAST_64 = 16
        private const val OFFSET_DESTINATION_ADDRESS_FIRST_64 = 24
        private const val OFFSET_DESTINATION_ADDRESS_LAST_64 = 32
    }

    internal val version: Int
        get() = packet.readByte(offset + OFFSET_VERSION).toInt() ushr 4

    internal var payloadLength: Int
        get() = packet.readShort(offset + OFFSET_PAYLOAD_LENGTH).toInt() and 0xFFFF
        set(value) = packet.writeShort(value.toShort(), offset + OFFSET_STANDARD_NEXT_HEADER)

    override val dataLength: Int get() = payloadLength + IPV6_STANDARD_LENGTH - headerLength

    internal val standardNextHeader: Int
        get() = packet.readByte(OFFSET_STANDARD_NEXT_HEADER).toInt() and 0xFF

    override val protocol: Byte

    internal val headerLength: Int

    init {
        val coreInfo = resolveTargetNextHeaderOffset(packet, offset, standardNextHeader)
        protocol = coreInfo.first.toByte()
        headerLength = coreInfo.second
    }

    internal var sourceAddress: IpAddress
        get() = IpAddress(
            IntIpv6(
                high64 = packet.readLong(offset + OFFSET_SOURCE_ADDRESS_FIRST_64),
                low64 = packet.readLong(offset + OFFSET_SOURCE_ADDRESS_LAST_64)
            )
        )
        set(value) {
            packet.writeLong(value.intIpv6.high64, offset + OFFSET_SOURCE_ADDRESS_FIRST_64)
            packet.writeLong(value.intIpv6.low64, offset + OFFSET_SOURCE_ADDRESS_LAST_64)
        }

    internal var destinationAddress: IpAddress
        get() = IpAddress(
            IntIpv6(
                high64 = packet.readLong(offset + OFFSET_DESTINATION_ADDRESS_FIRST_64),
                low64 = packet.readLong(offset + OFFSET_DESTINATION_ADDRESS_LAST_64)
            )
        )
        set(value) {
            packet.writeLong(value.intIpv6.high64, offset + OFFSET_DESTINATION_ADDRESS_FIRST_64)
            packet.writeLong(value.intIpv6.low64, offset + OFFSET_DESTINATION_ADDRESS_LAST_64)
        }

    override val addressSum: BigInteger
        get() = packet.calculateSum(offset + OFFSET_SOURCE_ADDRESS_FIRST_64, 32)

}