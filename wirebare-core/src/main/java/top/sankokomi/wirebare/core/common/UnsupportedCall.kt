package top.sankokomi.wirebare.core.common

/**
 * 标记此函数不应该由外部调用
 * */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class UnsupportedCall
