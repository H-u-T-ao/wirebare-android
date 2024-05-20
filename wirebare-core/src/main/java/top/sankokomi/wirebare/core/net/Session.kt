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

}