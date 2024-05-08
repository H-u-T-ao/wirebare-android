package top.sankokomi.wirebare.core.common

/**
 * 用来回调一些关键性的事件，通常是一些错误
 *
 * 这些错误往往不是致命的，但却非常重要，对于排查问题非常有帮助
 * */
interface IImportantEventListener {
    fun onPost(event: ImportantEvent)
}