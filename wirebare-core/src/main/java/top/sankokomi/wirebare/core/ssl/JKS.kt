package top.sankokomi.wirebare.core.ssl

import java.io.InputStream

class JKS(
    internal val sourceStream: () -> InputStream,
    internal val alias: String,
    internal val password: CharArray,
    internal val type: String = "PKCS12"
)