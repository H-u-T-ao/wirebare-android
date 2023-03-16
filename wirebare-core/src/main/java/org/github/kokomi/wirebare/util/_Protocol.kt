package org.github.kokomi.wirebare.util

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
        throw IllegalArgumentException("IP 地址格式错误 $this")
    }

internal val Short.convertPortToString: String
    get() = (this.toInt() and 0xFFFF).toString()

internal val Short.convertPortToInt: Int
    get() = this.toInt() and 0xFFFF