package conglin.clrpc.transfer.net.receiver;

import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import io.netty.channel.Channel;

public interface RequestReceiver{
    
    /**
     * 初始化
     * @param serviceHandler
     */
    public void init(ServerServiceHandler serviceHandler);


    /**
     * 处理请求
     * @param channel
     * @param request
     */
    public void handleRequest(Channel channel, BasicRequest request);
}