package top.sankokomi.wirebare.core.net

internal class TcpSessionStore : SessionStore<Port, TcpSession>() {

    /**
     * 添加或覆盖会话
     *
     * @return 添加成功的会话
     * */
    internal fun insert(
        sourcePort: Port,
        destinationAddress: IpAddress,
        destinationPort: Port
    ): TcpSession {
        val origin = query(sourcePort)
        return if (
            origin != null &&
            origin.destinationAddress == destinationAddress &&
            origin.destinationPort == destinationPort
        ) {
            origin
        } else {
            TcpSession(
                sourcePort, destinationAddress, destinationPort, this
            ).also { insertSession(sourcePort, it) }
        }
    }

}