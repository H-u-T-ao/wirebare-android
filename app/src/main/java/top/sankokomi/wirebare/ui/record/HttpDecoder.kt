package top.sankokomi.wirebare.ui.record

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.util.unzipBrotli
import top.sankokomi.wirebare.core.util.unzipGzip

private const val TAG = "HttpDecoder"

suspend fun decodeHttpBody(id: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val origin = getHttpRecordFileById(id)
            if (!origin.exists()) {
                return@withContext null
            } else {
                return@withContext origin.readBytes().httpBody()
            }
        }.onFailure {
            Log.e(TAG, "decodeHttpBody FAILED", it)
            return@withContext null
        }
        return@withContext null
    }
}

suspend fun decodeGzipHttpBody(id: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        runCatching {
            return@withContext decodeHttpBody(id)?.unzipGzip()
        }.onFailure {
            Log.e(TAG, "decodeGzipHttpBody FAILED", it)
            return@withContext null
        }
        return@withContext null
    }
}

suspend fun decodeBrotliHttpBody(id: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        runCatching {
            return@withContext decodeHttpBody(id)?.unzipBrotli()
        }.onFailure {
            Log.e(TAG, "decodeBrotliHttpBody FAILED", it)
            return@withContext null
        }
        return@withContext null
    }
}

suspend fun decodeBitmap(id: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val body = decodeHttpBody(id) ?: return@withContext null
            return@withContext BitmapFactory.decodeByteArray(body, 0, body.size)
        }.onFailure {
            Log.e(TAG, "decodeBitmap FAILED", it)
            return@withContext null
        }
        return@withContext null
    }
}

suspend fun decodeGzipBitmap(id: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val body = decodeGzipHttpBody(id) ?: return@withContext null
            return@withContext BitmapFactory.decodeByteArray(body, 0, body.size)
        }.onFailure {
            Log.e(TAG, "decodeGzipBitmap FAILED", it)
            return@withContext null
        }
        return@withContext null
    }
}

suspend fun decodeBrotliBitmap(id: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val body = decodeBrotliHttpBody(id) ?: return@withContext null
            return@withContext BitmapFactory.decodeByteArray(body, 0, body.size)
        }.onFailure {
            Log.e(TAG, "decodeBrotliBitmap FAILED", it)
            return@withContext null
        }
        return@withContext null
    }
}

private fun ByteArray.httpBody(): ByteArray? {
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
        return@httpBody bytes.copyOfRange(i, bytes.size)
    }.onFailure {
        return null
    }
    return null
}