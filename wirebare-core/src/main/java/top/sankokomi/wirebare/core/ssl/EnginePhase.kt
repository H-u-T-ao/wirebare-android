package top.sankokomi.wirebare.core.ssl

enum class EnginePhase {
    Initial,
    HandshakeStarted,
    HandshakeFinished,
    Ready,
    Closed,
    Unknown
}