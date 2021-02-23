package wiki.scene.huobiklinedemo.socket.entity

data class KLineInfo(
    var amount: Float = 0.0F,
    var close: Float = 0.0F,
    var count: Float = 0.0F,
    var high: Float = 0.0F,
    var id: Long = 0,
    var low: Float = 0.0F,
    var open: Float = 0.0F,
    var vol: Float = 0.0F
)