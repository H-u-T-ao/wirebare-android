package top.sankokomi.wirebare.core.util

import android.util.Log
import androidx.annotation.IntDef
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.net.Session
import top.sankokomi.wirebare.core.net.TcpSession
import top.sankokomi.wirebare.core.net.UdpSession

object Level {
    const val VERBOSE = 1
    const val DEBUG = 1 shl 1
    const val INFO = 1 shl 2
    const val WARN = 1 shl 3
    const val ERROR = 1 shl 4
    const val WTF = 1 shl 5
    const val SILENT = 1 shl 6
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
    internal var LOG_LEVEL: Int = Level.VERBOSE

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

    internal fun inet(session: TcpSession, msg: String) {
        debug("[${session.protocol.name}] ${WireBare.configuration.address}:${session.sourcePort} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

    internal fun inet(session: UdpSession, msg: String) {
        debug("[${session.protocol.name}] ${WireBare.configuration.address}:${session.sourcePort} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

}
