package top.sankokomi.wirebare.ui.record

import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.sankokomi.wirebare.core.interceptor.http.HttpRequest
import top.sankokomi.wirebare.core.interceptor.http.HttpResponse
import top.sankokomi.wirebare.ui.util.Global
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "HttpRecorder"

private val recordDir by lazy {
    File("${Global.appContext.externalCacheDir!!.absolutePath}${File.separator}http_record").also {
        if (!it.exists()) it.mkdirs()
    }
}

val HttpRequest.id: String get() = "req_${requestTime}_${hashCode()}"

val HttpResponse.id: String get() = "rsp_${requestTime}_${hashCode()}"

fun getHttpRecordFileById(id: String): File = File(recordDir, id)

object HttpRecorder {

    private val writers = ConcurrentHashMap<String, ConcurrentFileWriter>()

    fun parseRequestRecordFile(request: HttpRequest): File {
        val dest = File(recordDir, request.id)
        if (!dest.exists()) {
            dest.createNewFile()
        }
        return dest
    }

    fun parseResponseRecordFile(response: HttpResponse): File {
        val dest = File(recordDir, response.id)
        if (!dest.exists()) {
            dest.createNewFile()
        }
        return dest
    }

    suspend fun addRequestRecord(request: HttpRequest, buffer: ByteBuffer?) {
        withContext(Dispatchers.IO) {
            runCatching {
                val id = request.id
                if (buffer == null) {
                    writers.remove(id)?.close()
                    return@withContext
                }
                writers.computeIfAbsent(id) {
                    ConcurrentFileWriter(parseRequestRecordFile(request))
                }.writeBytes(buffer)
            }.onFailure {
                Log.e(TAG, "addHttpRequestReward FAILED", it)
            }
        }
    }

    suspend fun addResponseRecord(response: HttpResponse, buffer: ByteBuffer?) {
        withContext(Dispatchers.IO) {
            runCatching {
                val id = response.id
                if (buffer == null) {
                    writers.remove(id)?.close()
                    return@withContext
                }
                writers.computeIfAbsent(id) {
                    ConcurrentFileWriter(parseResponseRecordFile(response))
                }.writeBytes(buffer)
            }.onFailure {
                Log.e(TAG, "addHttpResponseReward FAILED", it)
            }
        }
    }

    suspend fun clearRewards() {
        withContext(Dispatchers.IO) {
            runCatching {
                recordDir.listFiles()?.forEach(File::delete)
            }.onFailure {
                Log.e(TAG, "clearHttpRewards FAILED", it)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun clearRewardsAsync() {
        GlobalScope.launch(Dispatchers.IO) {
            runCatching {
                recordDir.listFiles()?.forEach(File::delete)
            }.onFailure {
                Log.e(TAG, "clearHttpRewards FAILED", it)
            }
        }
    }

}