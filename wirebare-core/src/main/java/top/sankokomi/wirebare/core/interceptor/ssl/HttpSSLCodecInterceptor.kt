package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

class HttpSSLCodecInterceptor: HttpInterceptor {

    override fun onRequest(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        super.onRequest(chain, buffer, session)
    }

    override fun onRequestFinished(chain: HttpInterceptChain, session: TcpSession) {
        super.onRequestFinished(chain, session)
    }

    override fun onResponse(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
        super.onResponse(chain, buffer, session)
    }

    override fun onResponseFinished(chain: HttpInterceptChain, session: TcpSession) {
        super.onResponseFinished(chain, session)
    }

}