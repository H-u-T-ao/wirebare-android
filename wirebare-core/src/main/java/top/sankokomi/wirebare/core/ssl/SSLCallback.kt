package top.sankokomi.wirebare.core.ssl

import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine

interface SSLCallback {
    /**
     * 要等待数据包完整后再处理，下次回调时希望将两个数据包合并后再传入
     * */
    fun shouldPending(buffer: ByteBuffer) {}

    /**
     * 解密失败，通常是因为没有创建出 [SSLEngine]
     * */
    fun sslFailed(buffer: ByteBuffer) {}

    /**
     * 解密完成
     * */
    fun decryptSuccess(buffer: ByteBuffer) {}

    /**
     * 加密完成
     * */
    fun encryptSuccess(buffer: ByteBuffer) {}
}