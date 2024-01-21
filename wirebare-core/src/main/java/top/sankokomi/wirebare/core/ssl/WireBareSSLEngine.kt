package top.sankokomi.wirebare.core.ssl

import java.io.IOException
import java.nio.ByteBuffer
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult

class WireBareSSLEngine(private val engine: SSLEngine) {

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 20 * 1024
    }

    @Volatile
    private var phase: EnginePhase = EnginePhase.Initial

    fun decode(callback: DecodeCallback) {
        if (phase == EnginePhase.Closed) {
            throw IOException("SSL引擎已被关闭")
        }
        handshake(callback)

    }

    private fun handshake(callback: DecodeCallback) {
        if (phase == EnginePhase.Initial) {
            phase = EnginePhase.HandshakeStarted
            engine.beginHandshake()
        }
        var status = engine.handshakeStatus
        while (phase != EnginePhase.HandshakeFinished) {
            when (status) {
                SSLEngineResult.HandshakeStatus.NEED_WRAP -> {
                    handshakeWrap(callback)
                }

                SSLEngineResult.HandshakeStatus.NEED_UNWRAP -> {

                }

                SSLEngineResult.HandshakeStatus.NEED_TASK -> {

                }

                SSLEngineResult.HandshakeStatus.FINISHED -> {

                }

                else -> {
                    throw IOException("SSL引擎状态错误，非握手阶段")
                }
            }
        }
    }

    private fun handshakeWrap(callback: DecodeCallback): SSLEngineResult {
        var result: SSLEngineResult
        var output = ByteBuffer.allocate(DEFAULT_BUFFER_SIZE)
        while (true) {
            result = engine.wrap(ByteBuffer.allocate(0), output)
            val status = result.status
            output.flip()
            if (output.hasRemaining()) {
                callback.encodeSuccess(output)
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

}