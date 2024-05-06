package top.sankokomi.wirebare.core.service

import android.net.VpnService
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import top.sankokomi.wirebare.core.common.WireBareConfiguration
import top.sankokomi.wirebare.core.net.IIpHeader
import top.sankokomi.wirebare.core.net.IpHeader
import top.sankokomi.wirebare.core.net.IpVersion
import top.sankokomi.wirebare.core.net.Ipv4Header
import top.sankokomi.wirebare.core.net.Ipv6Header
import top.sankokomi.wirebare.core.net.Packet
import top.sankokomi.wirebare.core.net.Protocol
import top.sankokomi.wirebare.core.tcp.TcpPacketInterceptor
import top.sankokomi.wirebare.core.udp.UdpPacketInterceptor
import top.sankokomi.wirebare.core.util.WireBareLogger
import top.sankokomi.wirebare.core.util.closeSafely
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
        internal infix fun ProxyLauncher.dispatchWith(builder: VpnService.Builder) {
            val proxyDescriptor = builder.establish() ?: throw IllegalStateException(
                "请先准备代理服务"
            )
            PacketDispatcher(configuration, proxyDescriptor, proxyService).dispatch()
        }
    }

    /**
     * ip 包拦截器
     * */
    private val interceptors = hashMapOf<Protocol, PacketInterceptor>(
        Protocol.TCP to TcpPacketInterceptor(configuration, proxyService),
        Protocol.UDP to UdpPacketInterceptor(configuration, proxyService)
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
            runCatching {
                while (isActive) {
                    var length = 0
                    while (isActive) {
                        kotlin.runCatching {
                            // 从 VPN 服务中读取输入流
                            length = inputStream.read(buffer)
                        }.onFailure {
                            WireBareLogger.error(it)
                        }
                        if (length != 0) {
                            break
                        } else {
                            // 空闲了，等待 100 毫秒再继续轮询
                            delay(100L)
                        }
                    }

                    if (length <= 0) continue

                    // 添加到处理队列
                    pendingBuffers.offer(Packet(buffer, length))
                    // 新建新的缓冲区准备接收下一个字节包
                    buffer = ByteArray(configuration.mtu)
                }
            }.onFailure {
                if (it !is CancellationException) {
                    WireBareLogger.error(it)
                }
            }
            // 关闭所有资源
            closeSafely(proxyDescriptor, inputStream, outputStream)
        }
        // 启动协程对接收到的输入流进行处理并进行输出
        launch(Dispatchers.IO) {
            while (isActive) {
                var p: Packet? = null
                while (isActive) {
                    p = pendingBuffers.poll()
                    if (p != null) {
                        break
                    } else {
                        // 空闲了，等待 100 毫秒再继续轮询
                        delay(100L)
                    }
                }

                // 这里为空只有一种情况，那就是 isActive == false
                val packet = p ?: continue

                val ipHeader: IIpHeader
                when (val ipVersion = IpHeader.readIpVersion(packet, 0)) {
                    IpHeader.VERSION_4 -> {
                        if (packet.length < Ipv4Header.MIN_IPV4_LENGTH) {
                            WireBareLogger.warn("报文长度小于 ${Ipv4Header.MIN_IPV4_LENGTH}")
                            continue
                        }
                        ipHeader = Ipv4Header(packet.packet, 0)
                    }

                    IpHeader.VERSION_6 -> {
                        if (packet.length < Ipv6Header.IPV6_STANDARD_LENGTH) {
                            WireBareLogger.warn("报文长度小于 ${Ipv6Header.IPV6_STANDARD_LENGTH}")
                            continue
                        }
                        ipHeader = Ipv6Header(packet.packet, 0)
                    }

                    else -> {
                        WireBareLogger.debug("未知的 ip 版本号 0b${ipVersion.toString(2)}")
                        continue
                    }
                }

                val interceptor = interceptors[Protocol.parse(ipHeader.protocol)]
                if (interceptor == null) {
                    WireBareLogger.warn("未知的协议代号 0b${ipHeader.protocol.toString(2)}")
                    continue
                }

                kotlin.runCatching {
                    // 拦截器拦截输入流
                    when (ipHeader) {
                        is Ipv4Header -> interceptor.intercept(ipHeader, packet, outputStream)
                        is Ipv6Header -> interceptor.intercept(ipHeader, packet, outputStream)
                    }
                }.onFailure {
                    WireBareLogger.error(it)
                }
            }
        }
    }

}