package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

interface VirtualGateway {

    fun onRequest(buffer: ByteBuffer, session: TcpSession)

    fun onRequestFinished(session: TcpSession)

    fun onResponse(buffer: ByteBuffer, session: TcpSession)

    fun onResponseFinished(session: TcpSession)

}