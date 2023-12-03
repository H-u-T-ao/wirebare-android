package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.Interceptor
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

interface HttpInterceptor : Interceptor<HttpInterceptChain> {

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        chain.processRequestNext(buffer, session)
    }

    override fun onRequestFinished(chain: HttpInterceptChain, session: TcpSession) {
        chain.processRequestFinishedNext(session)
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        chain.processResponseNext(buffer, session)
    }

    override fun onResponseFinished(chain: HttpInterceptChain, session: TcpSession) {
        chain.processResponseFinishedNext(session)
    }

}