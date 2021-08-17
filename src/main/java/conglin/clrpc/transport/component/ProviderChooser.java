package conglin.clrpc.transport.component;

import conglin.clrpc.router.instance.ServiceInstance;
import conglin.clrpc.transport.message.BasicRequest;
import io.netty.channel.Channel;

import java.util.function.Predicate;

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
     * @param instancePredicate         指定的挑选条件
     * @return
     */
    Channel choose(String serviceName, Predicate<ServiceInstance> instancePredicate);
}