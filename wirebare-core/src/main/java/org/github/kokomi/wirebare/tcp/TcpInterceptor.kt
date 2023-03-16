package org.github.kokomi.wirebare.tcp

import org.github.kokomi.wirebare.common.WireBareConfiguration
import org.github.kokomi.wirebare.interceptor.VirtualGateWay
import org.github.kokomi.wirebare.net.*
import org.github.kokomi.wirebare.net.Port
import org.github.kokomi.wirebare.net.SessionStore
import org.github.kokomi.wirebare.net.TcpHeader
import org.github.kokomi.wirebare.service.PacketInterceptor
import org.github.kokomi.wirebare.service.WireBareProxyService
import org.github.kokomi.wirebare.util.WireBareLogger
import org.github.kokomi.wirebare.util.convertPortToInt
import java.io.OutputStream

/**
 * TCP 报文拦截器
 *
 * 拦截 IP 包并修改 [Ipv4Header.sourceAddress] 、
 * [TcpHeader.sourcePort] 、 [Ipv4Header.destinationAddress] 、
 * [TcpHeader.destinationPort]
 *
 * 目的是将被代理客户端的请求数据包代理到 [TcpProxyServer] ，
 * 并将远程服务器的响应数据包转发给被代理客户端
 * */
internal class TcpInterceptor(
    private val configuration: WireBareConfiguration,
    proxyService: WireBareProxyService
) : PacketInterceptor {

    private val sessionStore: SessionStore = SessionStore()

    private val tunIpv4Address = Ipv4Address(configuration.address)

    private val ports = hashSetOf<Port>()

    private val virtualGateWay: VirtualGateWay = VirtualGateWay()

    private val servers = mutableListOf<TcpProxyServer>().apply {
        for (i in 1..5) {
            val server = TcpProxyServer(
                sessionStore, virtualGateWay, configuration, proxyService
            )
            server.dispatch()
            ports.add(server.proxyServerPort)
            add(server)
        }
    }

    override fun intercept(
        ipv4Header: Ipv4Header, packet: Packet, outputStream: OutputStream
    ) {
        val tcpHeader = TcpHeader(ipv4Header, packet.packet, ipv4Header.headerLength)

        // 来源地址和端口
        val sourceAddress = ipv4Header.sourceAddress
        val sourcePort = tcpHeader.sourcePort

        // 目的地址和端口
        val destinationAddress = ipv4Header.destinationAddress
        val destinationPort = tcpHeader.destinationPort

        val logPrefix = if (tcpHeader.syn) {
            "[SYN]"
        } else if (tcpHeader.fin) {
            "[FIN]"
        } else if (tcpHeader.ack) {
            "[ACK] SEQ ${tcpHeader.sequenceNumber} ACK ${tcpHeader.acknowledgmentNumber}"
        } else {
            "[---]"
        }

        if (!ports.contains(sourcePort)) {
            sessionStore.insert(
                Protocol.TCP, sourcePort, destinationAddress, destinationPort
            )

            // 根据端口号分配给固定的服务器
            val proxyServerPort =
                servers[sourcePort.port.convertPortToInt % servers.size].proxyServerPort

            // 将被代理客户端的请求数据包转发给代理服务器
            ipv4Header.sourceAddress = destinationAddress

            ipv4Header.destinationAddress = tunIpv4Address
            tcpHeader.destinationPort = proxyServerPort

            WireBareLogger.error("$logPrefix 客户端 $sourcePort >> 代理服务器 $proxyServerPort ${tcpHeader.dataLength} 字节 PSH ${tcpHeader.psh}")
        } else {
            val session = sessionStore.query(destinationPort)
                ?: throw IllegalStateException("发现一个未建立会话但有响应的连接")

//            if (!session.active) session.drop()

            // 将远程服务器的响应数据包转发给被代理客户端
            ipv4Header.sourceAddress = destinationAddress
            tcpHeader.sourcePort = session.destinationPort

            ipv4Header.destinationAddress = tunIpv4Address

            WireBareLogger.error("$logPrefix 代理服务器 $sourcePort >> 客户端 $destinationPort ${tcpHeader.dataLength} 字节 PSH ${tcpHeader.psh}")
        }

        ipv4Header.notifyCheckSum()
        tcpHeader.notifyCheckSum()

        WireBareLogger.verbose(
            "$sourceAddress:$sourcePort >> $destinationAddress:$destinationPort\n" + "${ipv4Header.sourceAddress}:${tcpHeader.sourcePort} >> " + "${ipv4Header.destinationAddress}:${tcpHeader.destinationPort}"
        )

        outputStream.write(packet.packet, 0, packet.length)
    }

}