package org.github.kokomi.wirebare.util

import android.util.Log
import androidx.annotation.IntDef
import org.github.kokomi.wirebare.common.WireBare
import org.github.kokomi.wirebare.net.Session

object Level {
    const val VERBOSE = 1
    const val DEBUG = 2
    const val INFO = 4
    const val WARN = 8
    const val ERROR = 16
    const val WTF = 32
    const val SILENT = 64
}

@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.LOCAL_VARIABLE,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.VALUE_PARAMETER
)
@IntDef(value = [Level.VERBOSE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.WTF, Level.SILENT])
annotation class LogLevel

internal object WireBareLogger {

    private const val TAG = "WireBare"

    @LogLevel
    internal var LOG_LEVEL: Int = Level.SILENT

    internal fun verbose(msg: String) {
        if (LOG_LEVEL <= Level.VERBOSE) {
            Log.v(TAG, msg)
        }
    }

    internal fun debug(msg: String) {
        if (LOG_LEVEL <= Level.DEBUG) {
            Log.d(TAG, msg)
        }
    }

    internal fun info(msg: String) {
        if (LOG_LEVEL <= Level.INFO) {
            Log.i(TAG, msg)
        }
    }

    internal fun warn(msg: String) {
        if (LOG_LEVEL <= Level.WARN) {
            Log.w(TAG, msg)
        }
    }

    internal fun error(msg: String, cause: Throwable? = null) {
        if (LOG_LEVEL <= Level.ERROR) {
            Log.e(TAG, msg, cause)
        }
    }

    internal fun error(cause: Throwable? = null) {
        if (LOG_LEVEL <= Level.ERROR) {
            Log.e(TAG, cause?.message, cause)
        }
    }

    internal fun wtf(cause: Throwable) {
        if (LOG_LEVEL <= Level.WTF) {
            Log.wtf(TAG, cause)
        }
    }

    internal fun inet(session: Session, msg: String) {
        debug("[${session.protocol.name}] ${WireBare.configuration.address}:${session.sourcePort} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

}
