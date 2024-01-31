package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.common.UnsupportedCall
import top.sankokomi.wirebare.core.net.Session
import top.sankokomi.wirebare.core.util.clearAndPut
import java.nio.ByteBuffer
import java.util.Queue

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
     * @param target 需要直接发送的 [ByteBuffer]
     * */
    @UnsupportedCall
    fun processRequestFinial(target: ByteBuffer)

    /**
     * 不走剩余的所有拦截器，直接发送 [target]
     *
     * @param target 需要直接发送的 [ByteBuffer]
     * */
    @UnsupportedCall
    fun processResponseFinial(target: ByteBuffer)

    /**
     * 跳过原来需要发送的数据（原始数据包）
     * */
    @UnsupportedCall
    fun skipOriginBuffer()

    /**
     * 跳过拦截链，直接将 [target] 写到 [direction]
     * */
    @UnsupportedCall
    fun processExtraBuffer(target: ByteBuffer, direction: BufferDirection)

    @UnsupportedCall
    fun processResults(): Queue<Pair<ByteBuffer, BufferDirection>>
}