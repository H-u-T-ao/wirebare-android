package top.sankokomi.wirebare.ui.record

import android.util.Log
import okio.withLock
import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.locks.ReentrantLock

class ConcurrentFileWriter(
    private val file: File
) : Closeable {

    companion object {
        private const val TAG = "ConcurrentFileWriter"
    }

    private val output by lazy(LazyThreadSafetyMode.NONE) {
        BufferedOutputStream(
            FileOutputStream(file, true)
        )
    }

    private val lock = ReentrantLock(true)

    fun writeBytes(buffer: ByteBuffer) {
        lock.withLock {
            output.write(
                buffer.array(),
                buffer.position(),
                buffer.remaining()
            )
        }
    }

    override fun close() {
        runCatching {
            output.close()
        }.onFailure {
            Log.e(TAG, "close FAILED", it)
        }
    }

}