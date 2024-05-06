package top.sankokomi.wirebare.core.net

internal interface Ipv6ExtHeaderLengthResolver {
    fun nextHeaderToResolve(): Int
    fun resolveLength(packet: ByteArray, offset: Int): Int
}