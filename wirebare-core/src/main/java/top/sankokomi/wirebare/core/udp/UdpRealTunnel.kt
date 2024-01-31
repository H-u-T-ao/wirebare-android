package top.sankokomi.wirebare.core.udp

import android.net.VpnService
import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.net.Ipv4Header
import top.sankokomi.wirebare.core.net.UdpHeader
import top.sankokomi.wirebare.core.net.UdpSession
import top.sankokomi.wirebare.core.nio.DatagramSocketNioTunnel
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.closeSafely
import java.io.OutputStream
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.Selector

/**
 * UDP 代理客户端
 *
 * 负责与远程服务器通信，发送 UDP 数据包到远程服务器，并接收远程服务器返回的 UDP 数据包
 * */
internal class UdpRealTunnel(
    override val channel: DatagramChannel,
    override val selector: Selector,
    private val outputStream: OutputStream,
    private val session: UdpSession,
    udpHeader: UdpHeader,
    private val configuration: WireBareConfiguration,
    private val vpnService: VpnService
) : DatagramSocketNioTunnel() {

    private val header: UdpHeader = udpHeader.copyHeader.also {
        val localAddress = it.ipv4Header.sourceAddress
        val localPort = it.sourcePort
        val remoteAddress = it.ipv4Header.destinationAddress
        val remotePort = it.destinationPort
        it.ipv4Header.sourceAddress = remoteAddress
        it.sourcePort = remotePort
        it.ipv4Header.destinationAddress = localAddress
        it.destinationPort = localPort
    }

    internal fun connectRemoteServer(address: String, port: Int) {
        if (vpnService.protect(channel.socket())) {
            channel.configureBlocking(false)
            channel.connect(InetSocketAddress(address, port))
            prepareRead()
        } else {
            throw IllegalArgumentException("无法保护 UDP 通道的套接字")
        }
    }

    override fun onWrite(): Int {
        val length = super.onWrite()
        WireBareLogger.inetDebug(session, "代理客户端写入 $length 字节")
        return length
    }

    override fun onRead() {
        val buffer = ByteBuffer.allocate(configuration.mtu)
        val length = read(buffer)
        if (length < 0) {
            closeSafely()
        } else {
            WireBareLogger.inetDebug(session, "代理客户端读取 $length 字节")
            outputStream.write(createUdpMessage(buffer))
        }
    }

    /**
     * 创建 UDP 报文，此 UDP 报文的来源是远程服务器，目的地是被代理客户端，数据部分为 [buffer]
     * */
    private fun createUdpMessage(buffer: ByteBuffer): ByteArray {
        val arrayLength = header.ipv4Header.headerLength + 8 + buffer.remaining()

        val packet = ByteArray(arrayLength) {
            if (it < header.ipv4Header.headerLength + 8) {
                header.packet[it]
            } else {
                buffer[it - header.ipv4Header.headerLength - 8]
            }
        }

        val ipv4Header = Ipv4Header(packet, 0)
        val udpHeader = UdpHeader(ipv4Header, packet, ipv4Header.headerLength)

        ipv4Header.totalLength = arrayLength
        udpHeader.totalLength = arrayLength - ipv4Header.headerLength

        ipv4Header.notifyCheckSum()
        udpHeader.notifyCheckSum()

        return packet
    }

    override fun onException(t: Throwable) {
        close()
    }

    override fun close() {
        super.close()
        WireBareLogger.inetDebug(session, "UDP 代理结束")
    }

}