package top.sankokomi.wirebare.core.ssl

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.net.TcpSession
import java.security.KeyStore
import java.security.PrivateKey
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext

class SSLEngineFactory(
    private val configuration: WireBareConfiguration
) {

    companion object {
        private const val KEY_STORE_TYPE_JKS = "JKS"
        private const val SSL_PROTOCOL_TLS_V1_2 = "TLSv1.2"
        private const val SSL_PROTOCOL_TLS_V1 = "TLSv1"
    }

    private val sslContextMap = hashMapOf<String, SSLContext>()

    private fun requireSSLContext(host: String): SSLContext? {
        val cache = sslContextMap[host]
        if (cache != null) {
            return cache
        }
        val context = createSSLContext(host) ?: return null
        sslContextMap[host] = context
        return context
    }

    private fun createSSLContext(host: String): SSLContext? {
        val jks = configuration.jks ?: return null
        return kotlin.runCatching {
            SSLContext.getInstance(SSL_PROTOCOL_TLS_V1_2)
        }.onFailure {
            SSLContext.getInstance(SSL_PROTOCOL_TLS_V1)
        }.getOrNull()?.apply {
            val keyStore = KeyStore.getInstance(jks.type).also {
                it.load(jks.jksStream(), jks.password)
            }
            val ca = keyStore.getCertificate(jks.alias)
            val priKey = keyStore.getKey(jks.alias, jks.password) as PrivateKey
            val kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            ).also {
                it.init(
                    CertificateFactory.generateServer(host, jks, ca, priKey),
                    jks.password
                )
            }
            init(kmf.keyManagers, null, null)
        }
    }

    fun createClientSSLEngine(host: String): WireBareSSLEngine? {
        val engine = requireSSLContext(host)?.createSSLEngine() ?: return null
        val engineWrapper = WireBareSSLEngine(engine)
        engine.useClientMode = true
        engine.needClientAuth = false
        return engineWrapper
    }

    fun createServerSSLEngine(host: String): WireBareSSLEngine? {
        val engine = requireSSLContext(host)?.createSSLEngine() ?: return null
        val engineWrapper = WireBareSSLEngine(engine)
        engine.useClientMode = false
        engine.wantClientAuth = false
        engine.needClientAuth = false
        return engineWrapper
    }

}