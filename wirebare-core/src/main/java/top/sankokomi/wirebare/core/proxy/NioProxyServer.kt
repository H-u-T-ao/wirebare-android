package top.sankokomi.wirebare.core.proxy

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.nio.NioCallback
import java.nio.channels.Selector

/**
 * 适用于 NIO 的代理服务器的抽象，已经实现了 [Selector.select] 操作并进行对应回调
 *
 * @see [ProxyServer]
 * @see [NioCallback]
 * */
internal abstract class NioProxyServer : ProxyServer() {

    /**
     * NIO 的选择器，已经在 [process] 中对其完成了 [Selector.select] 操作并进行对应回调
     * */
    protected abstract val selector: Selector

    final override suspend fun process() {
        withContext(Dispatchers.IO) {
            var select = 0
            while(isActive) {
                select = selector.selectNow()
                if (select != 0) {
                    break
                } else {
                    delay(50L)
                }
            }
            if (select == 0) return@withContext
            val selectionKeys = selector.selectedKeys()
            var selectionKey = selectionKeys.firstOrNull()
            while (selectionKey != null) {
                val key = selectionKey
                selectionKeys.remove(key)
                selectionKey = selectionKeys.firstOrNull()
                val callback = key.attachment()
                if (!key.isValid || callback !is NioCallback) continue
                kotlin.runCatching {
                    if (key.isAcceptable) callback.onAccept()
                    else if (key.isConnectable) callback.onConnected()
                    else if (key.isReadable) callback.onRead()
                    else if (key.isWritable) callback.onWrite()
                }.onFailure {
                    callback.onException(it)
                }
            }
        }
    }

}