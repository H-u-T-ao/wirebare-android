package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.calculateSum
import top.sankokomi.wirebare.core.util.readByte
import top.sankokomi.wirebare.core.util.readInt
import top.sankokomi.wirebare.core.util.readShort
import top.sankokomi.wirebare.core.util.writeByte
import top.sankokomi.wirebare.core.util.writeShort
import java.math.BigInteger
import kotlin.experimental.and

/**
 * tcp 包头结构如下
 *
 *    0               1               2               3
 *    0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7 0 1 2 3 4 5 6 7
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |          Source Port          |       Destination Port        |  4
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                        Sequence Number                        |  8
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                    Acknowledgment Number                      | 12
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |       |       |C|E|U|A|P|R|S|F|                               |
 *    | Offset|  RES  |R|C|R|C|S|S|Y|I|            Window             | 16
 *    |       |       |W|E|G|K|H|T|N|N|                               |
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |           Checksum            |         Urgent Pointer        | 20
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                    Options                    |    Padding    | 24
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *    |                             data                              | 28
 *    +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * RES := Reserved
 */
internal class TcpHeader(
    private val ipHeader: IIpHeader,
    private val packet: ByteArray,
    private val offset: Int
) {

    companion object {
        private const val OFFSET_SOURCE_PORT = 0
        private const val OFFSET_DESTINATION_PORT = 2
        private const val OFFSET_SEQUENCE_NUMBER = 4
        private const val OFFSET_ACKNOWLEDGMENT_NUMBER = 8
        private const val OFFSET_OFFSET = 12
        private const val OFFSET_FLAG = 13
        private const val OFFSET_WINDOW = 14
        private const val OFFSET_CHECK_SUM = 16
        private const val MASK_FIN: Byte = 0b00000001
        private const val MASK_SYN: Byte = 0b00000010
        private const val MASK_RST: Byte = 0b00000100
        private const val MASK_PSH: Byte = 0b00001000
        private const val MASK_ACK: Byte = 0b00010000
        private const val MASK_URG: Byte = 0b00100000
        private const val MASK_ECE: Byte = 0b01000000
        private const val MASK_CRW: Byte = 0b10000000.toByte()
    }

    internal var sourcePort: Port
        get() = Port(packet.readShort(offset + OFFSET_SOURCE_PORT))
        set(value) = packet.writeShort(value.port, offset + OFFSET_SOURCE_PORT)

    internal var destinationPort: Port
        get() = Port(packet.readShort(offset + OFFSET_DESTINATION_PORT))
        set(value) = packet.writeShort(value.port, offset + OFFSET_DESTINATION_PORT)

    internal val sequenceNumber: Int
        get() = packet.readInt(offset + OFFSET_SEQUENCE_NUMBER)

    internal val acknowledgmentNumber: Int
        get() = packet.readInt(offset + OFFSET_ACKNOWLEDGMENT_NUMBER)

    internal val headerLength: Int
        get() = packet.readByte(offset + OFFSET_OFFSET).toInt() and 0xFF ushr 4 shl 2

    /**
     * 计算 tcp 包的数据部分的长度
     *
     * 计算结果 = [Ipv4Header.totalLength] - [Ipv4Header.headerLength] - [headerLength]
     * */
    internal val dataLength: Int
        get() = ipHeader.dataLength - headerLength

    internal var flag: Byte
        get() = packet.readByte(offset + OFFSET_FLAG)
        set(value) = packet.writeByte(value, offset + OFFSET_FLAG)

    internal val fin: Boolean get() = flag and MASK_FIN == MASK_FIN

    internal val syn: Boolean get() = flag and MASK_SYN == MASK_SYN

    internal val rst: Boolean get() = flag and MASK_RST == MASK_RST

    internal val psh: Boolean get() = flag and MASK_PSH == MASK_PSH

    internal val ack: Boolean get() = flag and MASK_ACK == MASK_ACK

    internal val urg: Boolean get() = flag and MASK_URG == MASK_URG

    internal val ece: Boolean get() = flag and MASK_ECE == MASK_ECE

    internal val crw: Boolean get() = flag and MASK_CRW == MASK_CRW

    internal var window: Int
        get() = packet.readShort(OFFSET_WINDOW).toInt() and 0xFFFF
        set(value) = packet.writeShort((value and 0xFFFF).toShort(), offset + OFFSET_CHECK_SUM)

    internal var checkSum: Short
        get() = packet.readShort(offset + OFFSET_CHECK_SUM)
        private set(value) = packet.writeShort(value, offset + OFFSET_CHECK_SUM)

    /**
     * 先将 tcp 头中的校验和置为 0 ，然后重新计算校验和
     * */
    internal fun notifyCheckSum() {
        checkSum = 0.toShort()
        checkSum = calculateChecksum()
    }

    private fun calculateChecksum(): Short {
        val totalLength = ipHeader.dataLength
        var sum = ipHeader.addressSum
        sum += BigInteger.valueOf((ipHeader.protocol.toInt() and 0xF).toLong())
        sum += BigInteger.valueOf(totalLength.toLong())
        sum += packet.calculateSum(offset, totalLength)
        var next = sum shr 16
        while (next != BigInteger.ZERO) {
            sum = (sum and BigInteger.valueOf(0xFFFF)) + next
            next = sum shr 16
        }
        return sum.inv().toShort()
    }

}