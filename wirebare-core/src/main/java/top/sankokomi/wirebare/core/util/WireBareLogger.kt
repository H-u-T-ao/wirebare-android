package top.sankokomi.wirebare.core.util

import android.util.Log
import androidx.annotation.IntDef
import top.sankokomi.wirebare.core.common.WireBare
import top.sankokomi.wirebare.core.net.IpVersion
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
    @Volatile
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

    internal fun warn(cause: Throwable? = null) {
        if (LOG_LEVEL <= Level.WARN) {
            Log.w(TAG, cause?.message, cause)
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

    internal fun inetVerbose(session: TcpSession, msg: String) {
        verbose("${tcpPrefix(session)} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

    internal fun inetDebug(session: TcpSession, msg: String) {
        debug("${tcpPrefix(session)} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

    internal fun inetInfo(session: TcpSession, msg: String) {
        info("${tcpPrefix(session)} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

    internal fun inetDebug(session: UdpSession, msg: String) {
        debug("[${session.protocol.name}] ${WireBare.configuration.ipv4Address}:${session.sourcePort} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

    internal fun inetInfo(session: UdpSession, msg: String) {
        info("[${session.protocol.name}] ${WireBare.configuration.ipv4Address}:${session.sourcePort} >> ${session.destinationAddress}:${session.destinationPort} $msg")
    }

    private fun tcpPrefix(session: TcpSession): String {
        return when (session.destinationAddress.ipVersion) {
            IpVersion.IPv4 -> "[IPv4-TCP] ${session.sourcePort}"
            IpVersion.IPv6 -> "[IPv6-TCP] ${session.sourcePort}"
        }
    }

}
