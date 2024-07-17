package top.sankokomi.wirebare.core.interceptor.http.async

import kotlinx.coroutines.runBlocking
import top.sankokomi.wirebare.core.common.IProxyStatusListener
import top.sankokomi.wirebare.core.common.ProxyStatus
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.util.deepCopy
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class AsyncHttpInterceptChain(
    private val interceptors: List<AsyncHttpInterceptor>
) : HttpInterceptor {

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processRequestFirst(buffer, session)
        super.onRequest(chain, buffer, session, tunnel)
    }

    override fun onRequestFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processRequestFinishedFirst(session)
        super.onRequestFinished(chain, session, tunnel)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processResponseFirst(buffer, session)
        super.onResponse(chain, buffer, session, tunnel)
    }

    override fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        processResponseFinishedFirst(session)
        super.onResponseFinished(chain, session, tunnel)
    }

    private val interceptorIndexMap =
        hashMapOf<AsyncHttpInterceptor, AsyncHttpInterceptor?>().also { map ->
            interceptors.forEachIndexed { index, interceptor ->
                map[interceptor] = interceptors.getOrNull(index + 1)
            }
        }

    /**
     * 处理请求体
     * */
    suspend fun processRequestNext(
        now: AsyncHttpInterceptor?,
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        nextInterceptor(now)?.onRequest(this, buffer, session)
    }

    /**
     * 请求体处理完毕
     * */
    suspend fun processRequestFinishedNext(
        now: AsyncHttpInterceptor?,
        session: HttpSession
    ) {
        nextInterceptor(now)?.onRequestFinished(this, session)
    }

    /**
     * 处理响应体
     * */
    suspend fun processResponseNext(
        now: AsyncHttpInterceptor?,
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        nextInterceptor(now)?.onResponse(this, buffer, session)
    }

    /**
     * 响应体处理完毕
     * */
    suspend fun processResponseFinishedNext(
        now: AsyncHttpInterceptor?,
        session: HttpSession
    ) {
        nextInterceptor(now)?.onResponseFinished(this, session)
    }

    private val executorPool = ThreadPoolExecutor(
        1,
        1,
        Long.MAX_VALUE,
        TimeUnit.DAYS,
        LinkedBlockingQueue()
    ).also {
        WireBare.addVpnProxyStatusListener(
            object : IProxyStatusListener {
                override fun onVpnStatusChanged(oldStatus: ProxyStatus, newStatus: ProxyStatus): Boolean {
                    if (newStatus == ProxyStatus.DEAD) {
                        it.shutdown()
                        return true
                    }
                    return false
                }
            }
        )
    }

    private fun processRequestFirst(
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        val buf = buffer.deepCopy()
        executorPool.execute {
            runBlocking {
                processRequestNext(null, buf, session)
            }
        }
    }

    private fun processRequestFinishedFirst(
        session: HttpSession
    ) {
        executorPool.execute {
            runBlocking {
                processRequestFinishedNext(null, session)
            }
        }
    }

    private fun processResponseFirst(
        buffer: ByteBuffer,
        session: HttpSession
    ) {
        val buf = buffer.deepCopy()
        executorPool.execute {
            runBlocking {
                processResponseNext(null, buf, session)
            }
        }
    }

    private fun processResponseFinishedFirst(
        session: HttpSession
    ) {
        executorPool.execute {
            runBlocking {
                processResponseFinishedNext(null, session)
            }
        }
    }

    private fun nextInterceptor(now: AsyncHttpInterceptor?): AsyncHttpInterceptor? {
        now ?: return interceptors.firstOrNull()
        return interceptorIndexMap[now]
    }
}