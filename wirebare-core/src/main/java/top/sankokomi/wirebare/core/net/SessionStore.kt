package top.sankokomi.wirebare.core.net

import java.util.concurrent.ConcurrentHashMap

internal class SessionStore {

    private val sessions: MutableMap<Port, Session> = ConcurrentHashMap(128)

    /**
     * 查找对应来源端口号的会话
     *
     * @return 若存在，则返回 [Session] ，否则返回 null
     * */
    internal fun query(port: Port): Session? {
        return sessions[port]
    }

    /**
     * 添加或覆盖会话
     *
     * @return 添加成功的会话
     * */
    internal fun insert(
        protocol: Protocol,
        sourcePort: Port,
        destinationAddress: Ipv4Address,
        destinationPort: Port
    ): Session {
        return Session(
            protocol, sourcePort, destinationAddress, destinationPort, this, true
        ).also { sessions[sourcePort] = it }
    }

    /**
     * 丢弃会话
     * */
    internal fun drop(session: Session): Session? {
        return sessions.remove(session.sourcePort)
    }

}