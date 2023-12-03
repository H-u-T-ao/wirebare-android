package top.sankokomi.wirebare.core.net

/**
 * 协议类
 *
 * @param name 协议名称
 * */
class Protocol private constructor(internal val name: String) {

    internal companion object {
        /**
         * 代表 TCP 协议
         * */
        val TCP = Protocol("TCP")

        /**
         * 代表 UDP 协议
         * */
        val UDP = Protocol("UDP")

        /**
         * 代表未知协议
         * */
        val NULL = Protocol("NULL")

        private val protocols = hashMapOf(
            6.toByte() to TCP,
            17.toByte() to UDP
        )

        /**
         * 根据协议对应的代码，取得对应协议
         *
         * @return 若支持该协议，则返回对应协议，否则返回 [NULL]
         *
         * @see [TCP]
         * @see [UDP]
         * @see [NULL]
         * */
        internal fun parse(code: Byte): Protocol {
            return protocols[code] ?: NULL
        }
    }

}