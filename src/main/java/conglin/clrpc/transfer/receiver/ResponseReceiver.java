package conglin.clrpc.transfer.receiver;

import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.transfer.message.BasicResponse;

public interface ResponseReceiver {

    /**
     * 初始化
     * @param serviceHandler
     */
    void init(ConsumerServiceHandler serviceHandler);


    /**
     * 处理回复
     * @param response
     */
    void handleResponse(BasicResponse response);

    /**
     * 关闭回复接收器
     */
    void destory();
}