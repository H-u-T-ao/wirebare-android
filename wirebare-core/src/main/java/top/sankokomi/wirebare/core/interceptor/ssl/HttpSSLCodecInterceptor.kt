package top.sankokomi.wirebare.core.interceptor.ssl

import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptChain
import top.sankokomi.wirebare.core.interceptor.http.HttpInterceptor
import top.sankokomi.wirebare.core.interceptor.http.HttpSession
import top.sankokomi.wirebare.core.interceptor.tcp.TcpTunnel
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.ssl.JKS
import top.sankokomi.wirebare.core.ssl.RequestSSLCodec
import top.sankokomi.wirebare.core.ssl.ResponseSSLCodec
import top.sankokomi.wirebare.core.ssl.SSLCallback
import top.sankokomi.wirebare.core.ssl.SSLEngineFactory
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.mergeBuffer
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

class HttpSSLCodecInterceptor(jks: JKS) : HttpInterceptor {

    private val factory = SSLEngineFactory(jks)

    internal val requestCodec = RequestSSLCodec(factory)

    internal val responseCodec = ResponseSSLCodec(factory)

    private val pendingReqCiphertextMap =
        hashMapOf<TcpSession, LinkedBlockingQueue<ByteBuffer>>()

    private val pendingRspCiphertextMap =
        hashMapOf<TcpSession, LinkedBlockingQueue<ByteBuffer>>()

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
        val pendingReqCiphertext = pendingReqCiphertext(tcpSession)
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
                    session.request.isPlaintext = true
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
        val pendingRspCiphertext = pendingRspCiphertext(tcpSession)
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
                    session.response.isPlaintext = true
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
        pendingReqCiphertextMap.remove(session.tcpSession)
    }

    override fun onResponseFinished(
        chain: HttpInterceptChain,
        session: HttpSession,
        tunnel: TcpTunnel
    ) {
        super.onResponseFinished(chain, session, tunnel)
        pendingRspCiphertextMap.remove(session.tcpSession)
    }

    private fun pendingReqCiphertext(
        tcpSession: TcpSession
    ): LinkedBlockingQueue<ByteBuffer> {
        return pendingReqCiphertextMap.computeIfAbsent(tcpSession) {
            LinkedBlockingQueue()
        }
    }

    private fun pendingRspCiphertext(
        tcpSession: TcpSession
    ): LinkedBlockingQueue<ByteBuffer> {
        return pendingRspCiphertextMap.computeIfAbsent(tcpSession) {
            LinkedBlockingQueue()
        }
    }
}