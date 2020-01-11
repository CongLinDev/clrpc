package conglin.clrpc.transport.component;

import conglin.clrpc.transport.message.BasicRequest;
import io.netty.channel.Channel;

public interface ProviderChooser {
    /**
     * 随机挑选
     * 
     * @param serviceName
     * @param request
     * @return
     */
    Channel choose(String serviceName, BasicRequest request);

    /**
     * 指定条件挑选
     * 
     * @param serviceName
     * @param key         指定的挑选条件
     * @return
     */
    Channel choose(String serviceName, String addition);
}