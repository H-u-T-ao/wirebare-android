package top.sankokomi.wirebare.ui.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.util.unzipGzip
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

fun deleteDataCache() {
    GlobalScope.launch(Dispatchers.IO) {
        runCatching {
            val p = Global.appContext.externalCacheDir!!
            if (p.exists()) {
                p.listFiles()?.forEach {
                    it?.delete()
                }
            }
        }.onFailure {
            Log.e("deleteDataCache", "FAILED", it)
        }
    }
}

fun appendToDataCache(fileName: String, buffer: ByteBuffer) {
    runCatching {
        val p = Global.appContext.externalCacheDir!!
        if (!p.exists()) {
            p.mkdirs()
        }
        val f = File(p, fileName)
        if (!f.exists()) {
            f.createNewFile()
        }

        // 创建一个FileOutputStream来写入文件
        val fileOutputStream = FileOutputStream(f, true)

        // 使用BufferedOutputStream进行缓冲以提高写入效率
        val bufferedOutputStream = BufferedOutputStream(fileOutputStream)

        // 将ByteArray写入到BufferedOutputStream中
        bufferedOutputStream.write(
            buffer.array(),
            buffer.position(),
            buffer.remaining()
        )

        // 刷新并关闭流
        bufferedOutputStream.flush()
        bufferedOutputStream.close()
    }.onFailure {
        Log.e("writeToDataCache", "FAILED", it)
    }
}

fun findHttpBody(bytes: ByteArray): ByteArray? {
    runCatching {
        var i = -1
        bytes.forEachIndexed { index, byte ->
            if (i == -1 &&
                index + 3 in bytes.indices &&
                byte == '\r'.code.toByte() &&
                bytes[index + 1] == '\n'.code.toByte() &&
                bytes[index + 2] == '\r'.code.toByte() &&
                bytes[index + 3] == '\n'.code.toByte()
            ) {
                i = index + 4
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

suspend fun decodeBodyBytes(fileName: String): ByteArray? {
    return withContext(Dispatchers.IO) {
        runCatching {
            val p = Global.appContext.externalCacheDir!!
            if (!p.exists()) {
                p.mkdirs()
            }
            val f = File(p, fileName)
            if (!f.exists()) {
                return@runCatching null
            } else {
                return@runCatching findHttpBody(f.readBytes())
            }
        }.onFailure {
            Log.e("getByteData", "FAILED", it)
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
            val p = Global.appContext.externalCacheDir!!
            if (!p.exists()) {
                p.mkdirs()
            }
            val f = File(p, fileName)
            if (!f.exists()) {
                return@runCatching null
            } else {
                return@runCatching findHttpBody(f.readBytes())?.unzipGzip()
            }
        }.onFailure {
            Log.e("getByteData", "FAILED", it)
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
        val body = decodeGzipBodyBytes(sessionId)?.unzipGzip() ?: return null
        return@runCatching BitmapFactory.decodeByteArray(body, 0, body.size)
    }.onFailure {
        return null
    }.onSuccess {
        return it
    }
    return null
}