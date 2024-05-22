package top.sankokomi.wirebare.core.util

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.zip.GZIPInputStream

fun ByteBuffer.unzipGzip(): ByteBuffer {
    val compressedData: ByteBuffer = this
    return ByteBuffer.wrap(compressedData.array().unzipGzip())
}

fun ByteArray.unzipGzip(): ByteArray {
    val compressedData: ByteArray = this
    val compressedStream = ByteArrayInputStream(compressedData)
    val gzipStream = GZIPInputStream(compressedStream)

    val decompressedOutputStream = ByteArrayOutputStream()

    val buffer = ByteArray(1024)
    var bytesRead: Int
    try {
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
    return decompressedOutputStream.toByteArray()
}