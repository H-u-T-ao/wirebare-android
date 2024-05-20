package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.ssl.RequestSSLCodec
import top.sankokomi.wirebare.core.ssl.ResponseSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.ssl.SSLEngineFactory
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.mergeBuffer
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

class HttpSSLCodecInterceptor(
    configuration: WireBareConfiguration
) : HttpInterceptor {

    private val factory = SSLEngineFactory(configuration)

    internal val requestCodec = RequestSSLCodec(factory)

    internal val responseCodec = ResponseSSLCodec(factory)

    private val pendingReqCiphertext = LinkedBlockingQueue<ByteBuffer>()

    private val pendingRspCiphertext = LinkedBlockingQueue<ByteBuffer>()

    override fun onRequest(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        val (request, _, tcpSession) = session
        if (request.isHttps != true) {
            super.onRequest(chain, buffer, session, tunnel)
            return
        }
        val host = request.hostInternal ?: return
        responseCodec.handshakeIfNecessary(
            tcpSession,
            host,
            object : SSLCallback {
                override fun encryptSuccess(target: ByteBuffer) {
                    tunnel.writeToRemoteServer(target)
                }
            }
        )
        pendingReqCiphertext.add(buffer)
        requestCodec.decode(
            tcpSession,
            host,
            pendingReqCiphertext.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingReqCiphertext.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    WireBareLogger.warn("SSL 引擎创建失败")
                }

                override fun decryptSuccess(target: ByteBuffer) {
                    session.isPlaintext = true
                    chain.processRequestNext(
                        this@HttpSSLCodecInterceptor,
                        target,
                        session,
                        tunnel
                    )
                }

                override fun encryptSuccess(target: ByteBuffer) {
                    tunnel.writeToLocalClient(target)
                }
            }
        )
    }

    override fun onResponse(
        chain: HttpInterceptChain,
        buffer: ByteBuffer,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        val (_, response, tcpSession) = session
        if (response.isHttps != true) {
            super.onResponse(chain, buffer, session, tunnel)
            return
        }
        val host = response.hostInternal ?: return
        pendingRspCiphertext.add(buffer)
        responseCodec.decode(
            tcpSession,
            host,
            pendingRspCiphertext.mergeBuffer(),
            object : SSLCallback {
                override fun shouldPending(target: ByteBuffer) {
                    pendingRspCiphertext.add(target)
                }

                override fun sslFailed(target: ByteBuffer) {
                    WireBareLogger.warn("SSL 引擎创建失败")
                    // chain.processRequestNext(buffer, session, tunnel)
                }

                override fun decryptSuccess(target: ByteBuffer) {
                    session.isPlaintext = true
                    chain.processResponseNext(
                        this@HttpSSLCodecInterceptor,
                        target,
                        session,
                        tunnel
                    )
                }

                override fun encryptSuccess(target: ByteBuffer) {
                    tunnel.writeToRemoteServer(target)
                }
            }
        )
    }

    override fun onRequestFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        super.onRequestFinished(chain, session, tunnel)
        pendingReqCiphertext.clear()
    }

    override fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        super.onResponseFinished(chain, session, tunnel)
        pendingRspCiphertext.clear()
    }
}