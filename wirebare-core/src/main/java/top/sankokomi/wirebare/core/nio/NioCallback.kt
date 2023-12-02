package top.sankokomi.wirebare.core.nio

interface NioCallback {

    /**
     * 连接完成时回调
     * */
    fun onConnected()

    /**
     * 接收到 Accept 事件时对调
     * */
    fun onAccept()

    /**
     * 接收到 Read 事件时对调
     * */
    fun onRead()

    /**
     * 接收到 Write 事件时对调
     *
     * @return 本次写入的字节总数
     * */
    fun onWrite(): Int

    /**
     * 抛出异常时回调
     * */
    fun onException(t: Throwable)

}