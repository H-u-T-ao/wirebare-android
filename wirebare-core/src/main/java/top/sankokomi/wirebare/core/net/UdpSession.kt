package top.sankokomi.wirebare.core.net

/**
 * 会话，存储请求/响应的信息
 *
 * @param sourcePort 会话的来源端口号
 * @param destinationAddress 会话的目的 ipv4 地址
 * @param destinationPort 会话的目的端口号
 * @param sessionStore 会话所对应的 [TcpSessionStore]
 * */
data class UdpSession internal constructor(
    val sourcePort: Port,
    val destinationAddress: Ipv4Address,
    val destinationPort: Port,
    internal val sessionStore: UdpSessionStore
) : Session<Port>(sessionStore) {

    override val protocol: Protocol = Protocol.UDP

    override val key: Port = sourcePort

    override fun hashCode(): Int {
        return sourcePort.port.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UdpSession

        if (sourcePort != other.sourcePort) return false

        return true
    }

}
