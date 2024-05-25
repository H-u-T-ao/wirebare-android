package top.sankokomi.wirebare.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.util.unzipGzip
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

private const val TAG = "CacheUtil"

private val cacheDir by lazy { Global.appContext.externalCacheDir!! }

suspend fun deleteCacheFiles() {
    withContext(Dispatchers.IO) {
        runCatching {
            if (cacheDir.exists()) {
                cacheDir.listFiles()?.forEach {
                    it?.delete()
                }
            }
        }.onFailure {
            Log.e(TAG, "deleteCacheFiles FAILED", it)
        }
    }
}

fun appendBufferToCacheFile(fileName: String, buffer: ByteBuffer) {
    runCatching {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        val f = File(cacheDir, fileName)
        if (!f.exists()) {
            f.createNewFile()
        }
        val fileOutputStream = FileOutputStream(f, true)
        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)
        bufferedOutputStream.write(
            buffer.array(),
            buffer.position(),
            buffer.remaining()
        )
        bufferedOutputStream.flush()
        bufferedOutputStream.close()
    }.onFailure {
        Log.e(TAG, "writeToDataCache FAILED", it)
    }
}

suspend fun decodeBodyBytes(fileName: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        runCatching {
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val f = File(cacheDir, fileName)
            if (!f.exists()) {
                return@runCatching null
            } else {
                return@runCatching f.readBytes().parseHttpBody()
            }
        }.onFailure {
            Log.e(TAG, "decodeBodyBytes FAILED", it)
            return@withContext null
        }.onSuccess {
            return@withContext it
        }
        return@withContext null
    }
}

suspend fun decodeGzipBodyBytes(fileName: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        runCatching {
            return@runCatching decodeBodyBytes(fileName)?.unzipGzip()
        }.onFailure {
            Log.e(TAG, "decodeGzipBodyBytes FAILED", it)
            return@withContext null
        }.onSuccess {
            return@withContext it
        }
        return@withContext null
    }
}

suspend fun decodeBitmap(sessionId: String): Bitmap? {
    runCatching {
        val body = decodeBodyBytes(sessionId) ?: return null
        return@runCatching BitmapFactory.decodeByteArray(body, 0, body.size)
    }.onFailure {
        return null
    }.onSuccess {
        return it
    }
    return null
}

suspend fun decodeGzipBitmap(sessionId: String): Bitmap? {
    runCatching {
        val body = decodeGzipBodyBytes(sessionId) ?: return null
        return@runCatching BitmapFactory.decodeByteArray(body, 0, body.size)
    }.onFailure {
        return null
    }.onSuccess {
        return it
    }
    return null
}

private fun ByteArray.parseHttpBody(): ByteArray? {
    runCatching {
        val bytes = this
        var i = -1
        for (index in 0..bytes.size - 4) {
            if (
                bytes[index] == '\r'.code.toByte() &&
                bytes[index + 1] == '\n'.code.toByte() &&
                bytes[index + 2] == '\r'.code.toByte() &&
                bytes[index + 3] == '\n'.code.toByte()
            ) {
                i = index + 4
                break
            }
        }
        return@runCatching bytes.copyOfRange(i, bytes.size)
    }.onFailure {
        return null
    }.onSuccess {
        return it
    }
    return null
}