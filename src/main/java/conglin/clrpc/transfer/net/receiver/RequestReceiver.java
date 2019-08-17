package conglin.clrpc.transfer.net.receiver;

import conglin.clrpc.common.util.threadpool.ThreadPool;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;

public interface RequestReceiver extends ThreadPool{
    
    /**
     * 初始化
     * @param serviceHandler
     */
    void init(ServerServiceHandler serviceHandler);


    /**
     * 处理请求
     * @param request
     * @return
     */
    BasicResponse handleRequest(BasicRequest request);

    /**
     * 关闭请求接收器
     */
    void stop();
}