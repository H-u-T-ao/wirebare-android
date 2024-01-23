package top.sankokomi.wirebare.core.interceptor

enum class BufferDirection {
    /**
     * 需要写回被代理客户端
     * */
    ProxyClient,
    /**
     * 需要写到远端服务器
     * */
    RemoteServer
}