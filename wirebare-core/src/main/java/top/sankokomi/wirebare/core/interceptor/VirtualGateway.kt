package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session
import java.nio.ByteBuffer
import java.util.Queue

/**
 * 虚拟网关
 * */
interface VirtualGateway<SESSION : Session<*>> {
    /**
     * ① 请求
     * */
    fun onRequest(
        buffer: ByteBuffer,
        session: SESSION
    ): Queue<Pair<ByteBuffer, BufferDirection>>

    /**
     * ④ 请求结束
     * */
    fun onRequestFinished(session: SESSION)

    /**
     * ② 响应
     * */
    fun onResponse(
        buffer: ByteBuffer,
        session: SESSION
    ): Queue<Pair<ByteBuffer, BufferDirection>>

    /**
     * ③ 响应结束
     * */
    fun onResponseFinished(session: SESSION)
}