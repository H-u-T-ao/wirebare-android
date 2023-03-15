package org.github.kokomi.wirebare.util

import java.io.Closeable

internal fun closeSafely(vararg closeables: Closeable) {
    for (closeable in closeables) {
        closeable.closeSafely()
    }
}

/**
 * 安全地关闭 [Closeable] 资源
 * */
internal fun Closeable.closeSafely() {
    kotlin.runCatching {
        close()
    }.onFailure {
        WireBareLogger.error(it)
    }
}