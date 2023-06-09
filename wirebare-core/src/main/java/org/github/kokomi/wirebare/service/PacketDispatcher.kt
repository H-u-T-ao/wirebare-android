package org.github.kokomi.wirebare.service

import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.github.kokomi.wirebare.common.WireBare
import org.github.kokomi.wirebare.common.WireBareConfiguration
import org.github.kokomi.wirebare.net.Ipv4Header
import org.github.kokomi.wirebare.net.Packet
import org.github.kokomi.wirebare.net.Protocol
import org.github.kokomi.wirebare.tcp.TcpInterceptor
import org.github.kokomi.wirebare.udp.UdpInterceptor
import org.github.kokomi.wirebare.util.WireBareLogger
import org.github.kokomi.wirebare.util.closeSafely
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.LinkedBlockingQueue

/**
 * ip 包调度者，负责从代理服务的输入流中获取 ip 包并根据 ip 头的信息分配给对应的 [PacketInterceptor]
 * */
internal class PacketDispatcher private constructor(
    private val configuration: WireBareConfiguration,
    private val proxyDescriptor: ParcelFileDescriptor,
    private val proxyService: WireBareProxyService
) : CoroutineScope by proxyService {

    companion object {
        internal infix fun ProxyLauncher.dispatchWith(
            builder: VpnService.Builder
        ) {
            val proxyDescriptor = builder.establish() ?: throw IllegalStateException(
                "请先调用 WireBare.prepareProxy() 准备代理服务"
            )
            PacketDispatcher(configuration, proxyDescriptor, proxyService).dispatch()
        }
    }

    /**
     * ip 包拦截器
     * */
    private val interceptors = hashMapOf<Protocol, PacketInterceptor>(
        Protocol.TCP to TcpInterceptor(configuration, proxyService),
        Protocol.UDP to UdpInterceptor(configuration, proxyService)
    )

    /**
     * 代理服务输入流
     * */
    private val inputStream = FileInputStream(proxyDescriptor.fileDescriptor)

    /**
     * 代理服务输出流
     * */
    private val outputStream = FileOutputStream(proxyDescriptor.fileDescriptor)

    /**
     * 缓冲流
     * */
    private var buffer = ByteArray(configuration.mtu)

    /**
     * 队列中等待处理的 ip 包
     * */
    private val pendingBuffers = LinkedBlockingQueue<Packet>()

    private fun dispatch() {
        if (!isActive) return
        // 启动协程接收 TUN 虚拟网卡的输入流
        launch(Dispatchers.IO) {
            while (isActive) {
                var length = 0
                kotlin.runCatching {
                    // 读取输入流
                    length = inputStream.read(buffer)
                }.onFailure {
                    WireBareLogger.error(it)
                }

                if (length == 0) continue

                // 添加到处理队列
                pendingBuffers.offer(Packet(buffer, length))
                buffer = ByteArray(configuration.mtu)
            }
            closeSafely(proxyDescriptor, inputStream, outputStream)
            WireBare.stopProxy()
        }
        // 启动协程对接收到的输入流进行处理并进行输出
        launch(Dispatchers.IO) {
            while (isActive) {
                val packet = pendingBuffers.take()

                if (packet.length < Ipv4Header.MIN_IPV4_LENGTH) {
                    WireBareLogger.warn("报文长度小于 ${Ipv4Header.MIN_IPV4_LENGTH}")
                    continue
                }

                val ipv4Header = Ipv4Header(packet.packet, 0)
                if (!ipv4Header.isIpv4) {
                    WireBareLogger.warn("未知的 ip 版本号 ${ipv4Header.version.toString(2)}")
                    continue
                }

                val interceptor = interceptors[Protocol.parse(ipv4Header.protocol)]
                if (interceptor == null) {
                    WireBareLogger.warn("未知的协议代号 ${ipv4Header.protocol.toString(2)}")
                    continue
                }

                kotlin.runCatching {
                    // 拦截器拦截输入流
                    interceptor.intercept(ipv4Header, packet, outputStream)
                }.onFailure {
                    WireBareLogger.error(it)
                }
            }
        }
    }

}