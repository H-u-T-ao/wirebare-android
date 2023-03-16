package org.github.kokomi.wirebare.udp

import org.github.kokomi.wirebare.common.WireBareConfiguration
import org.github.kokomi.wirebare.net.Ipv4Header
import org.github.kokomi.wirebare.net.Packet
import org.github.kokomi.wirebare.net.Protocol
import org.github.kokomi.wirebare.net.SessionStore
import org.github.kokomi.wirebare.net.UdpHeader
import org.github.kokomi.wirebare.service.PacketInterceptor
import org.github.kokomi.wirebare.service.WireBareProxyService
import org.github.kokomi.wirebare.util.WireBareLogger
import java.io.OutputStream

/**
 * UDP 报文拦截器
 *
 * 拦截被代理客户端的数据包并转发给 [UdpProxyServer]
 * */
internal class UdpInterceptor(
    configuration: WireBareConfiguration,
    proxyService: WireBareProxyService
) : PacketInterceptor {

    private val sessionStore: SessionStore = SessionStore()

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
            Protocol.UDP,
            sourcePort,
            destinationAddress,
            destinationPort
        )

        WireBareLogger.inet(session, "客户端 $sourcePort >> 代理服务器")

        proxyServer.proxy(ipv4Header, udpHeader, outputStream)
    }

}