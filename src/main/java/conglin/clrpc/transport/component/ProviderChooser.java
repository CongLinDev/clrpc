package conglin.clrpc.transport.component;

import conglin.clrpc.transport.message.BasicRequest;
import io.netty.channel.Channel;

public interface ProviderChooser {
    /**
     * 随机挑选
     * 
     * @param request
     * @return
     */
    Channel choose(BasicRequest request);

    /**
     * 指定条件挑选
     * 
     * @param serviceName
     * @param addition         指定的挑选条件
     * @return
     */
    Channel choose(String serviceName, String addition);
}