package top.sankokomi.wirebare.core.tcp

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.http.HttpVirtualGateway
import top.sankokomi.wirebare.core.net.Ipv4Address
import top.sankokomi.wirebare.core.net.Ipv4Header
import top.sankokomi.wirebare.core.net.Packet
import top.sankokomi.wirebare.core.net.Port
import top.sankokomi.wirebare.core.net.TcpSessionStore
import top.sankokomi.wirebare.core.net.TcpHeader
import top.sankokomi.wirebare.core.service.PacketInterceptor
import top.sankokomi.wirebare.core.service.WireBareProxyService
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.convertPortToInt
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

    private val sessionStore: TcpSessionStore = TcpSessionStore()

    /**
     * 虚拟网卡的 ip 地址，也就是代理服务器的 ip 地址
     * */
    private val tunIpv4Address = Ipv4Address(configuration.address)

    /**
     * 代理服务器的端口集合
     * */
    private val ports = hashSetOf<Port>()

    /**
     * 代理服务器
     * */
    private val servers = mutableListOf<TcpProxyServer>().apply {
        for (i in 1..configuration.tcpProxyServerCount) {
            val server = TcpProxyServer(
                sessionStore,
                HttpVirtualGateway(configuration),
                configuration,
                proxyService
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
            "[ACK]"
        } else {
            "[---]"
        }

        if (!ports.contains(sourcePort)) {
            // 来源不是代理服务器，说明该数据包是被代理客户端发出来的请求包
            sessionStore.insert(
                sourcePort, destinationAddress, destinationPort
            )

            // 根据端口号分配给固定的服务器
            val proxyServerPort = servers[
                sourcePort.port.convertPortToInt % servers.size
            ].proxyServerPort

            // 将被代理客户端的请求数据包转发给代理服务器
            ipv4Header.sourceAddress = destinationAddress

            ipv4Header.destinationAddress = tunIpv4Address
            tcpHeader.destinationPort = proxyServerPort

            WireBareLogger.debug("$logPrefix 客户端 $sourcePort >> 代理服务器 $proxyServerPort ${tcpHeader.dataLength} 字节")
        } else {
            // 来源是代理服务器，说明该数据包是响应包
            val session = sessionStore.query(
                destinationPort
            ) ?: throw IllegalStateException("发现一个未建立会话但有响应的连接")

            session.tryDrop()

            // 将远程服务器的响应包转发给被代理客户端
            ipv4Header.sourceAddress = destinationAddress
            tcpHeader.sourcePort = session.destinationPort

            ipv4Header.destinationAddress = tunIpv4Address

            WireBareLogger.debug("$logPrefix 代理服务器 $sourcePort >> 客户端 $destinationPort ${tcpHeader.dataLength} 字节")
        }

        ipv4Header.notifyCheckSum()
        tcpHeader.notifyCheckSum()

        outputStream.write(packet.packet, 0, packet.length)
    }

}