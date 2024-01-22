package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.net.TcpSession
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class SSLEngineFactory(
    private val configuration: WireBareConfiguration
) {

    companion object {
        private const val KEY_STORE_TYPE_JKS = "JKS"
        private const val SSL_PROTOCOL_TLS_V2 = "TLSv2"
        private const val SSL_PROTOCOL_TLS = "TLS"
    }

    private val sslContext: SSLContext? by lazy(::ensureSSLContext)

    private fun ensureSSLContext(): SSLContext? {
        val jks = configuration.jks ?: return null
        return kotlin.runCatching {
            SSLContext.getInstance(SSL_PROTOCOL_TLS_V2)
        }.onFailure {
            SSLContext.getInstance(SSL_PROTOCOL_TLS)
        }.getOrThrow().apply {
            val keyStore = KeyStore.getInstance(
                KEY_STORE_TYPE_JKS
            ).also {
                it.load(jks.sourceStream(), jks.password)
            }
            val kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            ).also {
                it.init(keyStore, jks.password)
            }
            init(kmf.keyManagers, null, null)
        }
    }

    private val clientSSLEngineMap = hashMapOf<String, WireBareSSLEngine>()

    private val serverSSLEngineMap = hashMapOf<String, WireBareSSLEngine>()

    fun createClientSSLEngine(session: TcpSession): WireBareSSLEngine? {
        var engineWrapper = clientSSLEngineMap[session.destinationAddress.string]
        if (engineWrapper != null) return engineWrapper
        val engine = sslContext?.createSSLEngine(
            session.destinationAddress.string,
            session.destinationPort.port.toInt()
        ) ?: return null
        engineWrapper = WireBareSSLEngine(engine)
        clientSSLEngineMap[session.destinationAddress.string] = engineWrapper
        engine.useClientMode = true
        engine.needClientAuth = false
        return engineWrapper
    }

    fun createServerSSLEngine(session: TcpSession): WireBareSSLEngine? {
        var engineWrapper = serverSSLEngineMap[session.destinationAddress.string]
        if (engineWrapper != null) return engineWrapper
        val engine = sslContext?.createSSLEngine(
            session.destinationAddress.string,
            session.destinationPort.port.toInt()
        ) ?: return null
        engineWrapper = WireBareSSLEngine(engine)
        serverSSLEngineMap[session.destinationAddress.string] = engineWrapper
        engine.useClientMode = false
        engine.needClientAuth = false
        return engineWrapper
    }

    fun dropClientSSLEngine(session: TcpSession) {
        clientSSLEngineMap.remove(session.destinationAddress.string)
    }

    fun dropServerSSLEngine(session: TcpSession) {
        clientSSLEngineMap.remove(session.destinationAddress.string)
    }

}