package org.github.kokomi.wirebare.nio

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.spi.AbstractSelectableChannel
import java.util.concurrent.ConcurrentLinkedDeque

/**
 * NIO 隧道抽象类，内部实现了一些 NIO 读写通用的操作
 * */
internal abstract class NioTunnel<SC : AbstractSelectableChannel> : NioCallback, Closeable {

    /**
     * NIO 通道
     * */
    internal abstract val channel: SC

    /**
     * 选择器
     * */
    internal abstract val selector: Selector

    /**
     * 标志此 [NioTunnel] 是否已经被关闭
     * */
    internal var isClosed: Boolean = false

    /**
     * 写入字节流的缓冲队列
     * */
    private val pendingBuffers = ConcurrentLinkedDeque<ByteBuffer>()

    /**
     * 复用此 [SelectionKey] 进行操作
     * */
    private var key: SelectionKey? = null

    /**
     * 从通道中读取字节流
     *
     * @param buffer 向此字节流写入读取的字节
     * @return 返回总读取的字节数，如果已经到流的末尾，则返回 -1
     * */
    protected abstract fun readByteBuffer(buffer: ByteBuffer): Int

    /**
     * 向通道中写入字节流
     *
     * @param buffer 此字节流包含将要写入的字节
     * @return 返回总写入的字节数
     * */
    protected abstract fun writeByteBuffer(buffer: ByteBuffer): Int

    override fun onConnected() {
    }

    override fun onAccept() {
    }

    override fun onRead() {
    }

    override fun onWrite(): Int {
        if (isClosed) return 0
        var length = 0
        var buffer = pendingBuffers.poll()
        while (buffer != null) {
            val flush = writeByteBuffer(buffer)
            length += flush
            if (buffer.remaining() > 0) {
                pendingBuffers.offer(buffer)
                return length
            }
            buffer = pendingBuffers.poll()
        }
        interestRead()
        return length
    }

    override fun onClosed() {
        close()
    }

    /**
     * 从 [channel] 中读取字节流到 [buffer] 中
     * */
    internal open fun read(buffer: ByteBuffer): Int {
        buffer.clear()
        val length = readByteBuffer(buffer)
        if (length >= 0) buffer.flip()
        return length
    }

    /**
     * 将 [buffer] 中的字节流添加到 [pendingBuffers] 中，并切换 [channel]
     * 为写状态，在 [selector] 中收到写事件后将会回调 [onWrite]
     * */
    internal open fun write(buffer: ByteBuffer) {
        if (!isClosed and buffer.hasRemaining()) {
            pendingBuffers.offer(buffer)
            interestWrite()
        }
    }

    /**
     * 初始化 [key] ，并注册读事件
     * */
    internal fun prepareRead() {
        if (channel.isBlocking) {
            channel.configureBlocking(false)
        }
        selector.wakeup()
        key = channel.register(selector, SelectionKey.OP_READ, this)
    }

    /**
     * 切换 [key] 为写操作
     * */
    private fun interestWrite() {
        selector.wakeup()
        key!!.interestOps(SelectionKey.OP_WRITE)
    }

    /**
     * 切换 [key] 为读操作
     * */
    private fun interestRead() {
        selector.wakeup()
        key!!.interestOps(SelectionKey.OP_READ)
    }

    /**
     * 关闭此 NIO 隧道，回收资源
     * */
    override fun close() {
        isClosed = true
        pendingBuffers.clear()
        channel.close()
    }

}