package top.sankokomi.wirebare.core.util

import org.brotli.dec.BrotliInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun ByteBuffer.unzipBrotli(): ByteBuffer {
    val compressedData: ByteBuffer = this
    return ByteBuffer.wrap(compressedData.array().unzipBrotli())
}

fun ByteArray.unzipBrotli(): ByteArray {
    val compressedData: ByteArray = this
    val compressedStream = ByteArrayInputStream(compressedData)
    val brotliStream = BrotliInputStream(compressedStream)
    val outputStream = ByteArrayOutputStream()
    try {
        brotliStream.copyTo(outputStream)
    } finally {
        try {
            brotliStream.close()
            compressedStream.close()
        } catch (ignored: Exception) {
        }
    }
    return outputStream.toByteArray()
}