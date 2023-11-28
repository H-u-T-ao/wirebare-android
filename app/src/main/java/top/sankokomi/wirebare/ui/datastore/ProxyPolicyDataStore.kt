package top.sankokomi.wirebare.ui.datastore

object ProxyPolicyDataStore : AppDataStore("proxy_policy") {
    val banAutoFilter by AppBooleanPref("ban_auto_filter", false)
}