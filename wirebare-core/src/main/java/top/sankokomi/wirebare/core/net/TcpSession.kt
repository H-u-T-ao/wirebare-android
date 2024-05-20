package top.sankokomi.wirebare.core.net

/**
 * 会话，存储请求/响应的信息
 *
 * @param sourcePort 会话的来源端口号
 * @param destinationAddress 会话的目的 ipv4 地址
 * @param destinationPort 会话的目的端口号
 * @param sessionStore 会话所对应的 [TcpSessionStore]
 * */
data class TcpSession internal constructor(
    val sourcePort: Port,
    val destinationAddress: IpAddress,
    val destinationPort: Port,
    internal val sessionStore: TcpSessionStore
) : Session<Port>(sessionStore) {

    override val protocol: Protocol = Protocol.TCP

    override val key: Port = sourcePort

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TcpSession

        if (sourcePort != other.sourcePort) return false
        if (destinationAddress != other.destinationAddress) return false
        return destinationPort == other.destinationPort
    }

    override fun hashCode(): Int {
        var result = sourcePort.hashCode()
        result = 31 * result + destinationAddress.hashCode()
        result = 31 * result + destinationPort.hashCode()
        return result
    }

    override fun toString(): String {
        return "{sourcePort = $sourcePort, " +
                "destinationAddress = $destinationAddress, " +
                "destinationPort = $destinationPort}"
    }
}
