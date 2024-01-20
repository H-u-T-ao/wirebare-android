package top.sankokomi.wirebare.core.interceptor.tcp

import top.sankokomi.wirebare.core.interceptor.Interceptor
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

interface TcpInterceptor<CHAIN: TcpInterceptChain<*, *>> : Interceptor<CHAIN, TcpSession> {

    override fun onRequest(chain: CHAIN, buffer: ByteBuffer, session: TcpSession) {
        chain.processRequestNext(buffer, session)
    }

    override fun onRequestFinished(chain: CHAIN, session: TcpSession) {
        chain.processRequestFinishedNext(session)
    }

    override fun onResponse(chain: CHAIN, buffer: ByteBuffer, session: TcpSession) {
        chain.processResponseNext(buffer, session)
    }

    override fun onResponseFinished(chain: CHAIN, session: TcpSession) {
        chain.processResponseFinishedNext(session)
    }

}