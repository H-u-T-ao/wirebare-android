package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.net.TcpSession

class RequestSSLCodec(
    private val engineFactory: SSLEngineFactory
) : SSLCodec() {

    private val serverSSLEngineMap = hashMapOf<TcpSession, WireBareSSLEngine>()

    override fun createSSLEngineWrapper(session: TcpSession, host: String): WireBareSSLEngine? {
        return serverSSLEngineMap[session] ?: let {
            engineFactory.createServerSSLEngine(host)?.also {
                serverSSLEngineMap[session] = it
                it.name = "REQ-$host:${session.destinationPort}"
            }
        }
    }
}