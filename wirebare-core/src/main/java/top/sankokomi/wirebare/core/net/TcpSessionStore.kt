package top.sankokomi.wirebare.core.net

internal class TcpSessionStore : SessionStore<Port, TcpSession>() {

    /**
     * 添加或覆盖会话
     *
     * @return 添加成功的会话
     * */
    internal fun insert(
        sourcePort: Port,
        destinationAddress: Ipv4Address,
        destinationPort: Port
    ): TcpSession {
        return TcpSession(
            sourcePort, destinationAddress, destinationPort, this
        ).also { insertSession(sourcePort, it) }
    }

}