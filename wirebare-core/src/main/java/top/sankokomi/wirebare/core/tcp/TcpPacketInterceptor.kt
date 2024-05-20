package top.sankokomi.wirebare.core.tcp

import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.interceptor.tcp.TcpVirtualGateway
import top.sankokomi.wirebare.core.net.IpAddress
import top.sankokomi.wirebare.core.net.IpVersion
import top.sankokomi.wirebare.core.net.Ipv4Header
import top.sankokomi.wirebare.core.net.Ipv6Header
import top.sankokomi.wirebare.core.net.Packet
import top.sankokomi.wirebare.core.net.Port
import top.sankokomi.wirebare.core.net.TcpHeader
import top.sankokomi.wirebare.core.net.TcpSessionStore
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
internal class TcpPacketInterceptor(
    private val configuration: WireBareConfiguration,
    proxyService: WireBareProxyService
) : PacketInterceptor {

    private val sessionStore: TcpSessionStore = TcpSessionStore()

    /**
     * 虚拟网卡的 ip 地址，也就是代理服务器的 ip 地址
     * */
    private val tunIpv4Address = IpAddress(
        configuration.ipv4Address,
        IpVersion.IPv4
    )

    private val tunIpv6Address = IpAddress(
        configuration.ipv6Address,
        IpVersion.IPv6
    )

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
                TcpVirtualGateway(configuration),
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

            WireBareLogger.info(
                "[IPv4-TCP] 客户端 $sourcePort >> 代理服务器 $proxyServerPort " +
                        "seq = ${tcpHeader.sequenceNumber.toUInt()} ack = ${tcpHeader.acknowledgmentNumber.toUInt()} " +
                        "flag = ${
                            tcpHeader.flag.toUByte().toString(2).padStart(6, '0')
                        } length = ${tcpHeader.dataLength}"
            )
        } else {
            // 来源是代理服务器，说明该数据包是响应包
            val session = sessionStore.query(destinationPort)
                ?: throw IllegalStateException(
                    "发现一个未建立会话但有响应的连接 端口 $destinationPort"
                )

//            if (tcpHeader.fin) {
//                session.tryDrop()
//            }

            // 将远程服务器的响应包转发给被代理客户端
            ipv4Header.sourceAddress = destinationAddress
            tcpHeader.sourcePort = session.destinationPort

            ipv4Header.destinationAddress = tunIpv4Address

            WireBareLogger.info(
                "[IPv4-TCP] 客户端 $destinationPort << 代理服务器 $sourcePort " +
                        "seq = ${tcpHeader.sequenceNumber.toUInt()} ack = ${tcpHeader.acknowledgmentNumber.toUInt()} " +
                        "flag = ${
                            tcpHeader.flag.toUByte().toString(2).padStart(6, '0')
                        } length = ${tcpHeader.dataLength}"
            )
        }

        ipv4Header.notifyCheckSum()
        tcpHeader.notifyCheckSum()

        outputStream.write(packet.packet, 0, packet.length)
    }

    override fun intercept(
        ipv6Header: Ipv6Header, packet: Packet, outputStream: OutputStream
    ) {
        val tcpHeader = TcpHeader(ipv6Header, packet.packet, ipv6Header.headerLength)

        // 来源地址和端口
        val sourceAddress = ipv6Header.sourceAddress
        val sourcePort = tcpHeader.sourcePort

        // 目的地址和端口
        val destinationAddress = ipv6Header.destinationAddress
        val destinationPort = tcpHeader.destinationPort

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
            ipv6Header.sourceAddress = destinationAddress

            ipv6Header.destinationAddress = tunIpv6Address
            tcpHeader.destinationPort = proxyServerPort

            WireBareLogger.info(
                "[IPv6-TCP] 客户端 $sourcePort >> 代理服务器 $proxyServerPort " +
                        "seq = ${tcpHeader.sequenceNumber.toUInt()} ack = ${tcpHeader.acknowledgmentNumber.toUInt()} " +
                        "flag = ${
                            tcpHeader.flag.toUByte().toString(2).padStart(6, '0')
                        } length = ${tcpHeader.dataLength}"
            )
        } else {
            // 来源是代理服务器，说明该数据包是响应包
            val session = sessionStore.query(destinationPort)
                ?: throw IllegalStateException(
                    "发现一个未建立会话但有响应的连接 端口 $destinationPort"
                )

//            if (tcpHeader.fin) {
//                session.tryDrop()
//            }

            // 将远程服务器的响应包转发给被代理客户端
            ipv6Header.sourceAddress = destinationAddress
            tcpHeader.sourcePort = session.destinationPort

            ipv6Header.destinationAddress = tunIpv6Address

            WireBareLogger.info(
                "[IPv6-TCP] 客户端 $destinationPort << 代理服务器 $sourcePort " +
                        "seq = ${tcpHeader.sequenceNumber.toUInt()} ack = ${tcpHeader.acknowledgmentNumber.toUInt()} " +
                        "flag = ${
                            tcpHeader.flag.toUByte().toString(2).padStart(6, '0')
                        } length = ${tcpHeader.dataLength}"
            )
        }

        // ipv4Header.notifyCheckSum()
        tcpHeader.notifyCheckSum()

        outputStream.write(packet.packet, 0, packet.length)
    }

}