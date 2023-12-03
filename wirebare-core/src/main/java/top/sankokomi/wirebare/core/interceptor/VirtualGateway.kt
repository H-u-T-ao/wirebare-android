package top.sankokomi.wirebare.core.interceptor

import top.sankokomi.wirebare.core.net.Session
import java.nio.ByteBuffer

interface VirtualGateway {

    fun onRequest(buffer: ByteBuffer, session: Session)

    fun onRequestFinished(session: Session)

    fun onResponse(buffer: ByteBuffer, session: Session)

    fun onResponseFinished(session: Session)

}