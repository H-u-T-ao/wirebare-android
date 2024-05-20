package top.sankokomi.wirebare.core.net

import java.util.concurrent.ConcurrentHashMap

abstract class SessionStore<K, S : Session<K>> {

    private val sessions: MutableMap<K, S> = ConcurrentHashMap(128)

    /**
     * 添加会话
     * */
    internal fun insertSession(key: K, session: S) {
        sessions[key] = session
    }

    /**
     * 查找会话
     *
     * @return 若存在，则返回，否则返回 null
     * */
    internal fun query(key: K): S? {
        return sessions[key]
    }

}