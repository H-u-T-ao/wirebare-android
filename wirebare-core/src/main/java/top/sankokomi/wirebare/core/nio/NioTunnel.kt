package top.sankokomi.wirebare.core.nio

import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.spi.AbstractSelectableChannel
import java.util.concurrent.LinkedBlockingQueue

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
    private val pendingBuffers = LinkedBlockingQueue<ByteBuffer>()

    /**
     * 复用此 [SelectionKey] 进行操作
     * */
    private lateinit var key: SelectionKey

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
        var total = 0
        while (!pendingBuffers.isEmpty()) {
            val buffer = pendingBuffers.poll() ?: break
            val remaining = buffer.remaining()
            // 将放入缓冲队列中的数据包通过套接字写出去
            val length = writeByteBuffer(buffer)
            total += length
            if (length < remaining) {
                // 等待下一次写
                pendingBuffers.offer(buffer)
                return total
            }
        }
        // 写完了，切为读操作，等待下一次读数据
        interestRead()
        return total
    }

    override fun onException(t: Throwable) {
    }

    /**
     * 从 [channel] 中读取字节流到 [buffer] 中
     * */
    internal open fun read(buffer: ByteBuffer): Int {
        buffer.clear()
        val length = readByteBuffer(buffer)
        if (length > 0) buffer.flip()
        return length
    }

    /**
     * 将 [buffer] 中的字节流添加到 [pendingBuffers] 中，并切换 [channel]
     * 为写状态，在 [selector] 中收到写事件后将会回调 [onWrite]
     * */
    internal open fun write(buffer: ByteBuffer) {
        if (!isClosed && buffer.hasRemaining()) {
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
        key.interestOps(SelectionKey.OP_WRITE)
    }

    /**
     * 切换 [key] 为读操作
     * */
    private fun interestRead() {
        selector.wakeup()
        key.interestOps(SelectionKey.OP_READ)
    }

    /**
     * 关闭此 NIO 隧道，回收资源
     * */
    override fun close() {
        pendingBuffers.clear()
        channel.close()
        isClosed = true
    }

}