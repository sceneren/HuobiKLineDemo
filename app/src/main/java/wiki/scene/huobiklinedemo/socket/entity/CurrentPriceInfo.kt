package wiki.scene.huobiklinedemo.socket.entity

data class CurrentPriceInfo(
    var ch: String = "",
    var tick: Tick = Tick(),
    var ts: Long = 0
)

data class Tick(
    var data: MutableList<Data> = mutableListOf(),
    var id: Long = 0,
    var ts: Long = 0
)

data class Data(
    var amount: Double = 0.0,
    var direction: String = "",
    var id: String = "",
    var price: Double = 0.0,
    var tradeId: Long = 0,
    var ts: Long = 0
)