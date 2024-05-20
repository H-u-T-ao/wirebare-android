package top.sankokomi.wirebare.core.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

fun ByteBuffer.unzipGzip(): ByteBuffer {
    val compressedData: ByteBuffer = this
    // 将ByteBuffer中的数据转换为InputStream
    val compressedStream = ByteArrayInputStream(compressedData.array())
    val gzipStream = GZIPInputStream(compressedStream)

    // 使用ByteArrayOutputStream来收集解压后的数据
    val decompressedOutputStream = ByteArrayOutputStream()

    // 使用缓冲区来读取和解压数据
    val buffer = ByteArray(1024)
    var bytesRead: Int
    try {
        // 读取和解压数据到输出流
        while (gzipStream.read(buffer).also { bytesRead = it } != -1) {
            decompressedOutputStream.write(buffer, 0, bytesRead)
        }
    } catch (e: IOException) {
        throw RuntimeException("Error decompressing gzip data", e)
    } finally {
        try {
            gzipStream.close()
            compressedStream.close()
        } catch (ignore: IOException) {
        }
    }
    // 将解压后的数据转换为ByteBuffer
    return ByteBuffer.wrap(decompressedOutputStream.toByteArray())
}