package conglin.clrpc.transfer.net.receiver;

import conglin.clrpc.common.util.threadpool.ThreadPool;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.message.BasicResponse;

public interface ResponseReceiver extends ThreadPool {

    /**
     * 初始化
     * @param serviceHandler
     */
    void init(ClientServiceHandler serviceHandler);


    /**
     * 处理回复
     * @param response
     */
    void handleResponse(BasicResponse response);

    /**
     * 关闭回复接收器
     */
    void stop();
}