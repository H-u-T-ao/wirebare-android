package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.readByte

internal fun resolveTargetNextHeaderOffset(
    packet: ByteArray,
    offset: Int,
    standardNextHeader: Int
): Pair<Int, Int> {
    var nextHeaderOffset = Ipv6Header.IPV6_STANDARD_LENGTH
    var nextHeader = standardNextHeader
    while (true) {
        if (Protocol.parse(nextHeader.toByte()) == Protocol.NULL) {
            val length = (resolverMap[nextHeader] ?: NormalExtHeaderLengthResolver).resolveLength(
                packet,
                offset + nextHeaderOffset
            )
            nextHeaderOffset += length
            nextHeader = runCatching {
                packet.readByte(nextHeaderOffset).toInt() and 0xFF
            }.getOrNull() ?: return 59 to 0
        } else {
            return nextHeader to nextHeaderOffset
        }
    }
}

private val resolverSet = hashSetOf<Ipv6ExtHeaderLengthResolver>()

private val resolverMap = hashMapOf<Int, Ipv6ExtHeaderLengthResolver>().also {
    for (resolver in resolverSet) {
        it[resolver.nextHeaderToResolve()] = resolver
    }
}

object NormalExtHeaderLengthResolver : Ipv6ExtHeaderLengthResolver {
    override fun nextHeaderToResolve(): Int {
        throw NotImplementedError()
    }

    override fun resolveLength(packet: ByteArray, offset: Int): Int {
        // 拓展头部长度(8 * ExtHeaderLength) + 2 字节（NextHeader 和 ExtHeaderLength）
        return (packet.readByte(offset + 1).toInt() and 0xFF) * 8 + 2
    }
}

//object HopByHopOptionsHeaderLengthResolver : NormalExtHeaderLengthResolver() {
//    override fun nextHeaderToResolve(): Int = 0
//}
//
//object DestinationOptionsHeaderLengthResolver : NormalExtHeaderLengthResolver() {
//    override fun nextHeaderToResolve(): Int = 60
//}
//
//object RoutingHeaderLengthResolver : NormalExtHeaderLengthResolver() {
//    override fun nextHeaderToResolve(): Int = 43
//}
//
//object FragmentHeaderLengthResolver : NormalExtHeaderLengthResolver() {
//    override fun nextHeaderToResolve(): Int = 44
//}