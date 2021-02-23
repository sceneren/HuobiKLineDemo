package wiki.scene.huobiklinedemo.socket;

import com.blankj.utilcode.util.GsonUtils;
import com.zhangke.websocket.SimpleDispatcher;
import com.zhangke.websocket.WebSocketHandler;
import com.zhangke.websocket.dispatcher.ResponseDelivery;
import com.zhangke.websocket.response.ErrorResponse;
import com.zhangke.websocket.response.Response;
import com.zhangke.websocket.response.ResponseFactory;

import org.greenrobot.eventbus.EventBus;

import java.nio.ByteBuffer;

import wiki.scene.huobiklinedemo.socket.event.MessageEvent;
import wiki.scene.huobiklinedemo.socket.request.ReqPongInfo;
import wiki.scene.huobiklinedemo.socket.response.ResPingInfo;
import wiki.scene.huobiklinedemo.util.GZipUtil;

/**
 * 消息分发器
 * <p>
 * Created by ZhangKe on 2018/6/27.
 */
public class AppResponseDispatcher extends SimpleDispatcher {

    /**
     * JSON 数据格式错误
     */
    public static final int JSON_ERROR = 11;
    /**
     * code 码错误
     */
    public static final int CODE_ERROR = 12;

    @Override
    public void onMessage(ByteBuffer byteBuffer, ResponseDelivery delivery) {
        String result = GZipUtil.uncompressToString(byteBuffer.array());
        try {
            if (result.startsWith("{\"ping")) {
                //接收到ping消息，回复pong消息
                ResPingInfo resPingInfo = GsonUtils.fromJson(result, ResPingInfo.class);
                if (resPingInfo != null && resPingInfo.getPing() != 0L) {
                    ReqPongInfo reqPongInfo = new ReqPongInfo(resPingInfo.getPing());
                    WebSocketHandler.getDefault().send(GsonUtils.toJson(reqPongInfo));
                }
            } else {
                //普通消息
                EventBus.getDefault().post(new MessageEvent(result));
            }
        } catch (Exception e) {
            ErrorResponse errorResponse = ResponseFactory.createErrorResponse();
            Response<String> textResponse = ResponseFactory.createTextResponse();
            textResponse.setResponseData(result);
            errorResponse.setResponseData(textResponse);
            errorResponse.setErrorCode(JSON_ERROR);
            errorResponse.setCause(e);
            onSendDataError(errorResponse, delivery);
        }
    }

    @Override
    public void onMessage(String message, ResponseDelivery delivery) {

    }

    /**
     * 统一处理错误信息，
     * 界面上可使用 ErrorResponse#getDescription() 来当做提示语
     */
    @Override
    public void onSendDataError(ErrorResponse error, ResponseDelivery delivery) {
        switch (error.getErrorCode()) {
            case ErrorResponse.ERROR_NO_CONNECT:
                error.setDescription("网络错误");
                break;
            case ErrorResponse.ERROR_UN_INIT:
                error.setDescription("连接未初始化");
                break;
            case ErrorResponse.ERROR_UNKNOWN:
                error.setDescription("未知错误");
                break;
            case JSON_ERROR:
                error.setDescription("数据格式异常");
                break;
            case CODE_ERROR:
                error.setDescription("响应码错误");
                break;
        }
        delivery.onSendDataError(error);
    }
}
