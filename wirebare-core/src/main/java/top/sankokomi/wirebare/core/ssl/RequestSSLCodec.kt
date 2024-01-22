package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.TcpSession

class RequestSSLCodec(
    private val engineFactory: SSLEngineFactory
): SSLCodec() {
    override fun createSSLEngineWrapper(session: TcpSession): WireBareSSLEngine? {
        return engineFactory.createServerSSLEngine(session)
    }
}