package org.github.kokomi.wirebare.udp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.github.kokomi.wirebare.common.WireBareConfiguration
import org.github.kokomi.wirebare.net.Ipv4Header
import org.github.kokomi.wirebare.net.Port
import org.github.kokomi.wirebare.net.SessionStore
import org.github.kokomi.wirebare.net.UdpHeader
import org.github.kokomi.wirebare.proxy.NioProxyServer
import org.github.kokomi.wirebare.service.WireBareProxyService
import org.github.kokomi.wirebare.util.WireBareLogger
import org.github.kokomi.wirebare.util.closeSafely
import org.github.kokomi.wirebare.util.convertPortToInt
import java.io.OutputStream
import java.nio.channels.DatagramChannel
import java.nio.channels.Selector

/**
 * UDP 代理服务器
 *
 * 负责启动 [UdpRealTunnel] 并让 [UdpRealTunnel] 与远程服务器通信
 * */
internal class UdpProxyServer(
    private val sessionStore: SessionStore,
    private val configuration: WireBareConfiguration,
    private val proxyService: WireBareProxyService
) : NioProxyServer(), CoroutineScope by proxyService {

    override val selector: Selector = Selector.open()

    private val tunnels = hashMapOf<Port, UdpRealTunnel>()

    /**
     * 开始代理 UDP 数据包
     * */
    internal fun proxy(
        ipv4Header: Ipv4Header,
        udpHeader: UdpHeader,
        outputStream: OutputStream
    ) {
        launch(Dispatchers.IO) {
            val sourcePort = udpHeader.sourcePort
            kotlin.runCatching {
                val tunnel =
                    tunnels[sourcePort] ?: createTunnel(ipv4Header, udpHeader, outputStream)
                tunnel.write(udpHeader.data)
            }.onFailure {
                tunnels.remove(sourcePort)?.closeSafely()
                WireBareLogger.error(it)
            }
        }
    }

    /**
     * 创建一个 [UdpRealTunnel]
     * */
    private fun createTunnel(
        ipv4Header: Ipv4Header,
        udpHeader: UdpHeader,
        outputStream: OutputStream
    ): UdpRealTunnel {
        val session = sessionStore.query(
            udpHeader.sourcePort
        ) ?: throw IllegalStateException("一个 UDP 请求因为找不到指定会话而代理失败")

        return UdpRealTunnel(
            DatagramChannel.open(),
            selector,
            outputStream,
            session,
            udpHeader,
            configuration,
            proxyService
        ).also {
            it.connectRemoteServer(
                ipv4Header.destinationAddress.string,
                udpHeader.destinationPort.port.convertPortToInt
            )
            tunnels[session.sourcePort] = it
        }
    }

    override fun release() {
        for (tunnel in tunnels.values) {
            tunnel.closeSafely()
        }
        tunnels.clear()
    }

}