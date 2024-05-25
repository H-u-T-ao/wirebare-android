package top.sankokomi.wirebare.core.interceptor.http

import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.ssl.RequestSSLCodec
import top.sankokomi.wirebare.core.ssl.ResponseSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.nio.ByteBuffer

class HttpFlushInterceptor(
    private val requestCodec: RequestSSLCodec? = null,
    private val responseCodec: ResponseSSLCodec? = null
) : HttpInterceptor {

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        if (session.request.isHttps == true && session.request.isPlaintext == true) {
            val (request, _, tcpSession) = session
            val host = request.hostInternal ?: return
            responseCodec?.encode(
                tcpSession,
                host,
                buffer,
                object : SSLCallback {
                    override fun encryptSuccess(target: ByteBuffer) {
                        tunnel.writeToRemoteServer(target)
                    }
                }
            ) ?: WireBareLogger.warn("HTTPS 请求报文被解密了但却没有编码器")
        } else {
            tunnel.writeToRemoteServer(buffer)
        }
        super.onRequest(chain, buffer, session, tunnel)
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        if (session.response.isHttps == true && session.response.isPlaintext == true) {
            val (_, response, tcpSession) = session
            val host = response.hostInternal ?: return
            requestCodec?.encode(
                tcpSession,
                host,
                buffer,
                object : SSLCallback {
                    override fun encryptSuccess(target: ByteBuffer) {
                        tunnel.writeToLocalClient(target)
                    }
                }
            ) ?: WireBareLogger.warn("HTTPS 响应报文被解密了但却没有编码器")
        } else {
            tunnel.writeToLocalClient(buffer)
        }
        super.onRequest(chain, buffer, session, tunnel)
    }

}