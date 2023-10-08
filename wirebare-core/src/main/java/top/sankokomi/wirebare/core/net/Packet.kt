package top.sankokomi.wirebare.core.net

/**
 * 数据包
 * */
internal class Packet(
    /**
     * 数据包的字节流
     * */
    internal val packet: ByteArray,
    /**
     * 数据包的长度
     * */
    internal val length: Int
)