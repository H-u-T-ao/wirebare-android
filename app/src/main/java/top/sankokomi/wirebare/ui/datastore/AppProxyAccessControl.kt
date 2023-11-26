package top.sankokomi.wirebare.ui.datastore

data class AppProxyAccessControl(
    /**
     * 应用包名
     * */
    val packageName: String,
    /**
     * 访问控制，true 表示允许代理，false 表示不允许
     * */
    val access: Boolean
)
