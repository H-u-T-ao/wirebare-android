package top.sankokomi.wirebare.core.ssl

import java.io.InputStream

class JKS(
    internal val jksStream: () -> InputStream,
    internal val alias: String,
    internal val password: CharArray,
    internal val type: String = "PKCS12",
    internal val organization: String = "WB",
    internal val organizationUnit: String = "WB"
)