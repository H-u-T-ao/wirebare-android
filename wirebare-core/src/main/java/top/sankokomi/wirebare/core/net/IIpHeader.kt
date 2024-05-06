package top.sankokomi.wirebare.core.net

import java.math.BigInteger

interface IIpHeader {
    val dataLength: Int
    val addressSum: BigInteger
    val protocol: Byte
}