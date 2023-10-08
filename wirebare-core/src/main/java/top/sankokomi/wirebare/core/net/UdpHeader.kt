package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.calculateSum
import top.sankokomi.wirebare.core.util.readShort
import top.sankokomi.wirebare.core.util.writeShort
import java.math.BigInteger
import java.nio.ByteBuffer

/**
 * udp 包头结构如下
 *
 *    0               1               2               3
 *    0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |          Source Port          |      Destination Port         | 4
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |            Length             |            Checksum           | 8
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * */
internal class UdpHeader(
    internal val ipv4Header: Ipv4Header,
    internal val packet: ByteArray,
    private val offset: Int
) {

    companion object {
        private const val OFFSET_SOURCE_PORT = 0
        private const val OFFSET_DESTINATION_PORT = 2
        private const val OFFSET_LENGTH = 4
        private const val OFFSET_CHECK_SUM = 6
    }

    internal var sourcePort: Port
        get() = Port(packet.readShort(offset + OFFSET_SOURCE_PORT))
        set(value) = packet.writeShort(value.port, offset + OFFSET_SOURCE_PORT)

    internal var destinationPort: Port
        get() = Port(packet.readShort(offset + OFFSET_DESTINATION_PORT))
        set(value) = packet.writeShort(value.port, offset + OFFSET_DESTINATION_PORT)

    internal var totalLength: Int
        get() = packet.readShort(offset + OFFSET_LENGTH).toInt()
        set(value) = packet.writeShort(value.toShort(), offset + OFFSET_LENGTH)

    internal var checkSum: Short
        get() = packet.readShort(offset + OFFSET_CHECK_SUM)
        private set(value) = packet.writeShort(value, offset + OFFSET_CHECK_SUM)

    /**
     * 复制一个与当前 udp 包的头一样的 udp 包，数据部分为空
     * */
    internal val copyHeader: UdpHeader
        get() {
            val array = ByteArray(ipv4Header.headerLength + 8) { packet[it] }.apply {
                writeShort(8.toShort(), offset + OFFSET_LENGTH)
            }
            return UdpHeader(
                Ipv4Header(array, 0).apply { totalLength = ipv4Header.headerLength + 8 },
                array,
                offset
            )
        }

    /**
     * 返回 udp 包的数据部分
     * */
    internal val data: ByteBuffer
        get() = ByteBuffer.wrap(packet, offset + 8, totalLength - 8)

    /**
     * 先将 udp 头中的校验和置为 0 ，然后重新计算校验和
     * */
    internal fun notifyCheckSum() {
        checkSum = 0.toShort()
        checkSum = calculateChecksum()
    }

    private fun calculateChecksum(): Short {
        val dataLength = ipv4Header.totalLength - ipv4Header.headerLength
        var sum = ipv4Header.ipv4AddressSum
        sum += BigInteger.valueOf((ipv4Header.protocol.toInt() and 0xF).toLong())
        sum += BigInteger.valueOf(dataLength.toLong())
        sum += packet.calculateSum(offset, dataLength)
        var next = sum shr 16
        while (next != BigInteger.ZERO) {
            sum = (sum and BigInteger.valueOf(0xFFFF)) + next
            next = sum shr 16
        }
        return sum.inv().toShort()
    }

}