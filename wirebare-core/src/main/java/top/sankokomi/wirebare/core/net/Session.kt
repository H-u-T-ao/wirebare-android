package top.sankokomi.wirebare.core.net

abstract class Session<K> protected constructor(
    private val sessionStore: SessionStore<K, *>
) {

    /**
     * 会话所属的协议
     * */
    abstract val protocol: Protocol

    /**
     * 唯一识别此会话的标志符
     * */
    abstract val key: K

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
            sessionStore.dropSession(key)
        }
    }

}