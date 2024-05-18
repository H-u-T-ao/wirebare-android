package top.sankokomi.wirebare.core.util

import top.sankokomi.wirebare.core.net.IntIpv6
import top.sankokomi.wirebare.core.net.IpVersion


/**
 * 将 [Int] 所代表的 IP 地址转换为 %s:%s:%s:%s 的形式
 * */
internal val Int.convertIpv4ToString: String
    get() = String.format(
        "%s.%s.%s.%s",
        this shr 24 and 0xFF,
        this shr 16 and 0xFF,
        this shr 8 and 0xFF,
        this and 0xFF
    )

internal val String.convertIpv4ToInt: Int
    get() = split(".").let { numbers ->
        kotlin.runCatching {
            return@let (numbers[0].toInt() and 0xFF shl 24) or
                    (numbers[1].toInt() and 0xFF shl 16) or
                    (numbers[2].toInt() and 0xFF shl 8) or
                    (numbers[3].toInt() and 0xFF)
        }
        throw IllegalArgumentException("IPv4 地址格式错误 $this")
    }

internal val IntIpv6.convertIpv6ToString: String
    get() = String.format(
        "%s:%s:%s:%s:%s:%s:%s:%s",
        (this.high64 shr 48 and 0xFFFF).toString(16),
        (this.high64 shr 32 and 0xFFFF).toString(16),
        (this.high64 shr 16 and 0xFFFF).toString(16),
        (this.high64 and 0xFFFF).toString(16),
        (this.low64 shr 48 and 0xFFFF).toString(16),
        (this.low64 shr 32 and 0xFFFF).toString(16),
        (this.low64 shr 16 and 0xFFFF).toString(16),
        (this.low64 and 0xFFFF).toString(16)
    )

internal val String.convertIpv6ToInt: IntIpv6
    get() = split(":").let { numbers ->
        kotlin.runCatching {
            return@let IntIpv6(
                (numbers[0].toLong(16) and 0xFFFF shl 48) or
                        (numbers[1].toLong(16) and 0xFFFF shl 32) or
                        (numbers[2].toLong(16) and 0xFFFF shl 16) or
                        (numbers[3].toLong(16) and 0xFFFF),
                (numbers[4].toLong(16) and 0xFFFF shl 48) or
                        (numbers[5].toLong(16) and 0xFFFF shl 32) or
                        (numbers[6].toLong(16) and 0xFFFF shl 16) or
                        (numbers[7].toLong(16) and 0xFFFF)
            )
        }
        throw IllegalArgumentException("IPv6 地址格式错误 $this")
    }

internal val Short.convertPortToString: String
    get() = (this.toInt() and 0xFFFF).toString()

internal val Short.convertPortToInt: Int
    get() = this.toInt() and 0xFFFF

internal val String.ipVersion: IpVersion?
    get() {
        runCatching {
            this.convertIpv4ToInt
            return IpVersion.IPv4
        }.onFailure {
            return IpVersion.IPv6
        }
        return null
    }