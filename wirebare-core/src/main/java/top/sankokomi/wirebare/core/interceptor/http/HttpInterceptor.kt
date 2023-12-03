package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.Interceptor
import top.sankokomi.wirebare.core.net.Session
import java.nio.ByteBuffer

interface HttpInterceptor : Interceptor<HttpInterceptChain> {

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: Session) {
        chain.processRequestNext(buffer, session)
    }

    override fun onRequestFinished(chain: HttpInterceptChain, session: Session) {
        chain.processRequestFinishedNext(session)
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: Session) {
        chain.processResponseNext(buffer, session)
    }

    override fun onResponseFinished(chain: HttpInterceptChain, session: Session) {
        chain.processResponseFinishedNext(session)
    }

}