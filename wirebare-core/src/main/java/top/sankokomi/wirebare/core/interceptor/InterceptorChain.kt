package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.common.UnsupportedCall
import top.sankokomi.wirebare.core.net.Session
import top.sankokomi.wirebare.core.util.clearAndPut
import java.nio.ByteBuffer

/**
 * 拦截器责任链
 * */
interface InterceptorChain<SESSION : Session<*>> {

    /**
     * 处理请求体
     * */
    fun processRequestNext(buffer: ByteBuffer, session: SESSION)

    /**
     * 请求体处理完毕
     * */
    fun processRequestFinishedNext(session: SESSION)

    /**
     * 处理响应体
     * */
    fun processResponseNext(buffer: ByteBuffer, session: SESSION)

    /**
     * 响应体处理完毕
     * */
    fun processResponseFinishedNext(session: SESSION)

    @UnsupportedCall
    fun processRequestFirst(buffer: ByteBuffer, session: SESSION)

    @UnsupportedCall
    fun processRequestFinishedFirst(session: SESSION)

    @UnsupportedCall
    fun processResponseFirst(buffer: ByteBuffer, session: SESSION)

    @UnsupportedCall
    fun processResponseFinishedFirst(session: SESSION)

    /**
     * 不走剩余的所有拦截器，直接发送 [target]
     *
     * @param origin 在拦截链中拦截到的 [ByteBuffer]
     * @param target 需要直接发送的 [ByteBuffer]
     * */
    @UnsupportedCall
    fun processRequestFinial(origin: ByteBuffer, target: ByteBuffer) {
        origin.clearAndPut(target)
    }

    /**
     * 不走剩余的所有拦截器，直接发送 [target]
     *
     * @param origin 在拦截链中拦截到的 [ByteBuffer]
     * @param target 需要直接发送的 [ByteBuffer]
     * */
    @UnsupportedCall
    fun processResponseFinial(origin: ByteBuffer, target: ByteBuffer) {
        origin.clearAndPut(target)
    }

    /**
     * 跳过请求拦截链，直接将 [target] 写回被代理客户端
     * */
    @UnsupportedCall
    fun skipRequestAndReflux(target: ByteBuffer)

    /**
     * 跳过响应拦截链，直接将 [target] 写到远端服务器
     * */
    @UnsupportedCall
    fun skipResponseAndReflux(target: ByteBuffer)

    @UnsupportedCall
    fun processRequestResult(): Pair<ByteBuffer, BufferDirection>?

    @UnsupportedCall
    fun processResponseResult(): Pair<ByteBuffer, BufferDirection>?
}