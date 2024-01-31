package top.sankokomi.wirebare.core.ssl

import android.os.Build
import top.sankokomi.wirebare.core.util.WireBareLogger
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult

class WireBareSSLEngine(private val engine: SSLEngine) {

    companion object {
        private const val TAG = "WireBareSSLEngine"

        private const val DEFAULT_BUFFER_SIZE = 20 * 1024
        private const val SSL_CONTENT_TYPE_HANDSHAKE = 22
    }

    internal var name: String = ""

    @Volatile
    private var phase: EnginePhase = EnginePhase.Initial

    private val pendingPlaintext = ConcurrentLinkedQueue<ByteBuffer>()

    fun decodeBuffer(
        input: ByteBuffer,
        callback: SSLCallback
    ) {
        if (phase == EnginePhase.Closed) {
            throw IOException("SSL引擎已被关闭")
        }
        if (phase == EnginePhase.HandshakeFinished) {
            val shouldRenegotiation = input[input.position()].toInt() == SSL_CONTENT_TYPE_HANDSHAKE
            unwrap(input, callback)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                // 在 API 27+ 版本上，系统自带的 SSLEngine 不支持重新协商
                if (shouldRenegotiation) {
                    handshakeWrap(callback)
                }
            }
        } else {
            handshake(input, callback)
        }
        // 握手完成了，此时有待发送的数据的话，立即发送
        if (phase == EnginePhase.HandshakeFinished && !pendingPlaintext.isEmpty()) {
            while (!pendingPlaintext.isEmpty()) {
                val plaintextBuffer = pendingPlaintext.poll()
                if (plaintextBuffer != null && plaintextBuffer.hasRemaining()) {
                    wrap(plaintextBuffer, callback)
                }
            }
        }
    }

    fun encodeBuffer(
        input: ByteBuffer,
        callback: SSLCallback
    ) {
        if (!input.hasRemaining()) {
            return
        }
        if (phase == EnginePhase.HandshakeFinished) {
            wrap(input, callback)
        } else {
            pendingPlaintext.offer(input)
        }
    }

    private fun wrap(
        input: ByteBuffer,
        callback: SSLCallback
    ) {
        var output: ByteBuffer = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        while (true) {
            val result: SSLEngineResult = engine.wrap(input, output)
            WireBareLogger.verbose("[$TAG-$name] wrap $result")
            val status = result.status
            output.flip()
            if (output.hasRemaining()) {
                callback.encryptSuccess(output)
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                output = ByteBuffer.allocate(engine.session.applicationBufferSize)
            } else {
                if (status == SSLEngineResult.Status.CLOSED) {
                    phase = EnginePhase.Closed
                }
                break
            }
        }
        if (phase != EnginePhase.Closed && input.hasRemaining()) {
            wrap(input, callback)
        }
    }

    private fun unwrap(
        input: ByteBuffer,
        callback: SSLCallback
    ) {
        var output: ByteBuffer? = null
        while (true) {
            if (output == null) {
                output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
            }
            val result: SSLEngineResult = engine.unwrap(input, output)
            WireBareLogger.verbose("[$TAG-$name] unwrap $result")
            val status = result.status
            output!!.flip()
            val producedSize = output.remaining()
            if (producedSize > 0) {
                callback.decryptSuccess(output)
                output = null
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                var bufferSize = engine.session.applicationBufferSize - producedSize
                if (bufferSize < 0) {
                    bufferSize = engine.session.applicationBufferSize
                }
                output = ByteBuffer.allocate(bufferSize)
            } else if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                if (input.hasRemaining()) {
                    callback.shouldPending(
                        ByteBuffer.wrap(
                            input.array(), input.position(),
                            input.remaining()
                        )
                    )
                    input.position(0)
                    input.limit(0)
                }
                break
            } else if (status == SSLEngineResult.Status.CLOSED) {
                phase = EnginePhase.Closed
                break
            } else {
                if (!input.hasRemaining()) {
                    break
                }
            }
        }
    }

    /**
     * 开始进行 SSl 握手协商
     * */
    internal fun handshake(
        input: ByteBuffer,
        callback: SSLCallback
    ) {
        if (phase == EnginePhase.Initial) {
            phase = EnginePhase.HandshakeStarted
            engine.beginHandshake()
        }
        var status = engine.handshakeStatus
        while (phase != EnginePhase.HandshakeFinished) {
            WireBareLogger.verbose("[$TAG-$name] handshake $status")
            when (status) {
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                    status = handshakeWrap(callback).handshakeStatus
                }

                SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> {
                    if (!input.hasRemaining()) {
                        break
                    }
                    status = handshakeUnwrap(input, callback).handshakeStatus
                }

                SSLEngineResult.HandshakeStatus.NEED_TASK -> {
                    runDelegatedTasks()
                }

                SSLEngineResult.HandshakeStatus.FINISHED -> {
                    phase = EnginePhase.HandshakeFinished
                    if (input.hasRemaining()) {
                        // 握手完毕，但是却还有数据没被发送
                        decodeBuffer(input, callback)
                    }
                }

                else -> {
                    throw IOException("SSL引擎状态错误，非握手阶段")
                }
            }
        }
    }

    private fun runDelegatedTasks() {
        while (true) {
            engine.delegatedTask?.run() ?: break
        }
    }

    private fun handshakeWrap(callback: SSLCallback): SSLEngineResult {
        var result: SSLEngineResult
        var output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        while (true) {
            result = engine.wrap(ByteBuffer.allocate(0), output)
            val status = result.status
            output.flip()
            if (output.hasRemaining()) {
                callback.encryptSuccess(output)
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                output = ByteBuffer.allocate(engine.session.applicationBufferSize)
            } else {
                if (status == SSLEngineResult.Status.CLOSED) {
                    phase = EnginePhase.Closed
                }
                break
            }
        }
        return result
    }

    private fun handshakeUnwrap(
        input: ByteBuffer,
        callback: SSLCallback
    ): SSLEngineResult {
        var result: SSLEngineResult
        var output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        while (true) {
            result = engine.unwrap(input, output)
            val status = result.status
            output.flip()
            val producedSize = output.remaining()
            if (producedSize > 0) {
                callback.decryptSuccess(output)
            }
            if (status == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                var bufferSize = engine.session.applicationBufferSize - producedSize
                if (bufferSize < 0) {
                    bufferSize = engine.session.applicationBufferSize
                }
                output = ByteBuffer.allocate(bufferSize)
            } else if (status == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
                if (input.hasRemaining()) {
                    callback.shouldPending(
                        ByteBuffer.wrap(
                            input.array(), input.position(),
                            input.remaining()
                        )
                    )
                    input.position(0)
                    input.limit(0)
                }
                break
            } else if (status == SSLEngineResult.Status.CLOSED) {
                phase = EnginePhase.Closed
                break
            } else {
                break
            }
        }
        return result
    }

}