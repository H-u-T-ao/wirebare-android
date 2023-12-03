package top.sankokomi.wirebare.core.net

/**
 * 会话，存储请求/响应的信息
 *
 * @param protocol 会话的协议
 * @param sourcePort 会话的来源端口号
 * @param destinationAddress 会话的目的 ipv4 地址
 * @param destinationPort 会话的目的端口号
 * @param sessionStore 会话所对应的 [SessionStore]
 * */
data class Session internal constructor(
    val protocol: Protocol,
    val sourcePort: Port,
    val destinationAddress: Ipv4Address,
    val destinationPort: Port,
    internal val sessionStore: SessionStore
) {

    private var dying: Boolean = false

    /**
     * 标记此会话已经濒死，下一次被转发时即可抛弃
     * */
    internal fun markDying() {
        dying = true
    }

    /**
     * 若此会话已经濒死，则抛弃此会话
     * */
    internal fun tryDrop() {
        if (dying) {
            sessionStore.drop(this)
        }
    }

}
