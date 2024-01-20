package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

/**
 * 虚拟网关
 * */
interface VirtualGateway<SESSION : Session<*>> {

    fun onRequest(buffer: ByteBuffer, session: SESSION)

    fun onRequestFinished(session: SESSION)

    fun onResponse(buffer: ByteBuffer, session: SESSION)

    fun onResponseFinished(session: SESSION)

}