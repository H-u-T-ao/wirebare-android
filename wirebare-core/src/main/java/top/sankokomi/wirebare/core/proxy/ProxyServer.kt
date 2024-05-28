package top.sankokomi.wirebare.core.proxy

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 代理服务器，是最简单的抽象，只有负责进行处理数据和释放资源的函数
 * */
internal abstract class ProxyServer : CoroutineScope {

    /**
     * 只要协程仍然存活，此函数将一直被执行来进行代理服务器的调度
     * */
    protected abstract suspend fun process()

    /**
     * 协程被取消后，此函数用于释放资源
     * */
    protected abstract fun release()

    /**
     * 开始调度代理服务器
     * */
    internal fun dispatch() {
        launch(Dispatchers.IO) {
            while (isActive) {
                process()
            }
            release()
        }
    }

}