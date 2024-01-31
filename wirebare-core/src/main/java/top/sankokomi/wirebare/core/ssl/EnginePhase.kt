package top.sankokomi.wirebare.core.ssl

enum class EnginePhase {
    Initial,
    HandshakeStarted,
    HandshakeFinished,
    Closed,
    Unknown
}