package wiki.scene.huobiklinedemo.socket.response

import wiki.scene.huobiklinedemo.socket.entity.KLineInfo

data class BaseSocketResponseList(
    var errorCode: Int = 0,
    var cause: String? = null,
    var requestData: String? = null,
    var requestText: String? = null,
    val responseData: String? = null,
    val description: String? = null,
    val id: String? = null,
    var data: MutableList<KLineInfo> = mutableListOf()
)