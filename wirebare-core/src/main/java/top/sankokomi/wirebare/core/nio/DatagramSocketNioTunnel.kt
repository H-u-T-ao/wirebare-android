package top.sankokomi.wirebare.core.nio

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel

/**
 * udp 套接字的 [NioTunnel] 实现
 * */
internal abstract class DatagramSocketNioTunnel : NioTunnel<DatagramChannel>(), Closeable {

    /**
     * udp 套接字通道
     * */
    abstract override val channel: DatagramChannel

    final override fun onConnected() {
        throw IllegalStateException("UDP 不是面向连接的通信，不要激活可连接操作")
    }

    override fun readByteBuffer(buffer: ByteBuffer): Int = channel.read(buffer)

    override fun writeByteBuffer(buffer: ByteBuffer): Int = channel.write(buffer)

}