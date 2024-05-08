package top.sankokomi.wirebare.core.common

class ImportantEvent(
    val message: String,
    val synopsis: EventSynopsis,
    val cause: Throwable?
)