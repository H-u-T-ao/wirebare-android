package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.TcpSession

class ResponseSSLCodec(
    private val engineFactory: SSLEngineFactory
): SSLCodec() {
    override fun createSSLEngineWrapper(session: TcpSession): WireBareSSLEngine? {
        return engineFactory.createClientSSLEngine(session)
    }
}