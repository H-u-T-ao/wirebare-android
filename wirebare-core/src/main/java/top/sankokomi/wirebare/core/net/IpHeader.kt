package top.sankokomi.wirebare.core.net

import top.sankokomi.wirebare.core.util.readByte

internal object IpHeader {

    const val VERSION_4 = 0b0100
    const val VERSION_6 = 0b0110

    internal fun readIpVersion(
        packet: Packet,
        offset: Int
    ): Int {
        return packet.packet.readByte(offset).toInt() ushr 4
    }

    internal fun Int.toIpVersion(): IpVersion? {
        return when (this) {
            VERSION_4 -> IpVersion.IPv4
            VERSION_6 -> IpVersion.IPv6
            else -> null
        }
    }

}