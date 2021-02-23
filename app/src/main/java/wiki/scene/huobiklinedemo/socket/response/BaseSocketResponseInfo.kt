package wiki.scene.huobiklinedemo.socket.response

import wiki.scene.huobiklinedemo.socket.entity.KLineInfo

data class BaseSocketResponseInfo(
    val errorCode: Int,
    val cause: String,
    val requestData: String,
    val requestText: String,
    val responseData: String,
    val description: String,
    val id: String,
    val data: KLineInfo
)