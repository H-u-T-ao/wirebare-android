package top.sankokomi.wirebare.core.service

import top.sankokomi.wirebare.core.net.Ipv4Header
import top.sankokomi.wirebare.core.net.Ipv6Header
import top.sankokomi.wirebare.core.net.Packet
import java.io.OutputStream

/**
 * ipv4 包拦截器，可以对 ip 包进行修改和发送
 * */
internal interface PacketInterceptor {

    /**
     * 拦截 ipv4 包
     *
     * @param ipv4Header ipv4 头
     * @param packet ip 包
     * @param outputStream 代理服务的输出流
     * */
    fun intercept(
        ipv4Header: Ipv4Header,
        packet: Packet,
        outputStream: OutputStream
    )

    /**
     * 拦截 ipv6 包
     *
     * @param ipv6Header ipv6 头
     * @param packet ip 包
     * @param outputStream 代理服务的输出流
     * */
    fun intercept(
        ipv6Header: Ipv6Header,
        packet: Packet,
        outputStream: OutputStream
    ) {}

}