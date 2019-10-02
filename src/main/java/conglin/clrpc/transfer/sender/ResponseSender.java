package conglin.clrpc.transfer.sender;

import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;

public interface ResponseSender {
    /**
     * 发送回复
     * @param channel
     * @param response
     */
    void sendResponse(Channel channel, BasicResponse response);

    /**
     * 关闭发送器
     */
    void destory();
}