package org.github.kokomi.wirebare.net

/**
 * 会话，存储请求/响应的信息
 *
 * @param protocol 会话的协议
 * @param sourcePort 会话的来源端口号
 * @param destinationAddress 会话的目的 ipv4 地址
 * @param destinationPort 会话的目的端口号
 * @param sessionStore 会话所对应的 [SessionStore]
 * @param active 会话是否仍然存活
 * */
internal data class Session(
    internal val protocol: Protocol,
    internal val sourcePort: Port,
    internal val destinationAddress: Ipv4Address,
    internal val destinationPort: Port,
    private val sessionStore: SessionStore,
    internal var active: Boolean
) {

    /**
     * 若此会话已经死亡，则丢弃此会话
     * */
    internal fun drop() {
        sessionStore.drop(this)
    }

}
