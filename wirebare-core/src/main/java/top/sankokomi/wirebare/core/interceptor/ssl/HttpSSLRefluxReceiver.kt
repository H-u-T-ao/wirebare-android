package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

interface HttpSSLRefluxReceiver {

    fun onRequestReflux(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
    }

    fun onResponseReflux(chain: HttpInterceptChain, buffer: ByteBuffer, session: TcpSession) {
    }

}