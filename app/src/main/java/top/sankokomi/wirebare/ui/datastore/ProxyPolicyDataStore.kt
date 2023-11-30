package top.sankokomi.wirebare.ui.datastore

object ProxyPolicyDataStore : AppDataStore("proxy_policy") {

    /**
     * true：禁用自动过滤，false：启用自动过滤
     * */
    val banAutoFilter by AppBooleanPref("ban_auto_filter")

    /**
     * true：显示系统应用，false：不显示系统应用
     * */
    val showSystemApp by AppBooleanPref("ban_system_app")
}