package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.TcpSession
import java.nio.ByteBuffer

class ResponseSSLCodec(
    private val engineFactory: SSLEngineFactory
) : SSLCodec() {

    private val clientSSLEngineMap = hashMapOf<TcpSession, WireBareSSLEngine>()

    override fun createSSLEngineWrapper(session: TcpSession, host: String): WireBareSSLEngine? {
        return clientSSLEngineMap[session] ?: (engineFactory.createClientSSLEngine(host)
            ?.also { clientSSLEngineMap[session] = it })
    }

    internal fun handshakeIfNecessary(
        session: TcpSession,
        host: String,
        callback: SSLCallback
    ) {
        createSSLEngineWrapper(session, host)?.handshake(
            ByteBuffer.allocate(0),
            callback
        )
    }
}