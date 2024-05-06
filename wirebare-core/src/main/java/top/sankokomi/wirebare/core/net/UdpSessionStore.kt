package top.sankokomi.wirebare.core.net

internal class UdpSessionStore : SessionStore<Port, UdpSession>() {

    /**
     * 添加或覆盖会话
     *
     * @return 添加成功的会话
     * */
    internal fun insert(
        sourcePort: Port,
        destinationAddress: IpAddress,
        destinationPort: Port
    ): UdpSession {
        return UdpSession(
            sourcePort, destinationAddress, destinationPort, this
        ).also { insertSession(sourcePort, it) }
    }

}