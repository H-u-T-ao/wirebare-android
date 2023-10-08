package top.sankokomi.wirebare.core.nio

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

/**
 * tcp 套接字的 [NioTunnel] 实现
 * */
internal abstract class SocketNioTunnel : NioTunnel<SocketChannel>(), Closeable {

    /**
     * tcp 套接字通道
     * */
    abstract override val channel: SocketChannel

    override fun readByteBuffer(buffer: ByteBuffer): Int = channel.read(buffer)

    override fun writeByteBuffer(buffer: ByteBuffer): Int = channel.write(buffer)

}