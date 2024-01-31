package top.sankokomi.wirebare.core.udp

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.net.Ipv4Header
import top.sankokomi.wirebare.core.net.Packet
import top.sankokomi.wirebare.core.net.UdpHeader
import top.sankokomi.wirebare.core.net.UdpSessionStore
import top.sankokomi.wirebare.core.service.PacketInterceptor
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.io.OutputStream

/**
 * UDP 报文拦截器
 *
 * 拦截被代理客户端的数据包并转发给 [UdpProxyServer]
 * */
internal class UdpPacketInterceptor(
    configuration: WireBareConfiguration,
    proxyService: WireBareProxyService
) : PacketInterceptor {

    private val sessionStore: UdpSessionStore = UdpSessionStore()

    private val proxyServer =
        UdpProxyServer(sessionStore, configuration, proxyService).apply { dispatch() }

    override fun intercept(
        ipv4Header: Ipv4Header,
        packet: Packet,
        outputStream: OutputStream
    ) {
        val udpHeader = UdpHeader(ipv4Header, packet.packet, ipv4Header.headerLength)

        val sourcePort = udpHeader.sourcePort

        val destinationAddress = ipv4Header.destinationAddress
        val destinationPort = udpHeader.destinationPort

        val session = sessionStore.insert(
            sourcePort,
            destinationAddress,
            destinationPort
        )

        WireBareLogger.inetDebug(session, "客户端 $sourcePort >> 代理服务器")

        proxyServer.proxy(ipv4Header, udpHeader, outputStream)
    }

}