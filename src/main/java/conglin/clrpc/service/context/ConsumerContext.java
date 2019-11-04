package conglin.clrpc.service.context;

import java.util.function.BiFunction;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.executor.AbstractConsumerServiceExecutor;
import conglin.clrpc.service.executor.RequestSender;
import conglin.clrpc.service.executor.ServiceExecutor;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;

public interface ConsumerContext extends CommonContext {
    /**
     * 设置执行器(包括发送器)
     * @param consumerServiceExecutor
     */
    void setConsumerServiceExecutor(AbstractConsumerServiceExecutor consumerServiceExecutor);

    /**
     * 获取执行器
     * @return
     */
    ServiceExecutor<BasicResponse> getServiceExecutor();

    /**
     * 获取发送器
     * @return
     */
    RequestSender getRequestSender();


    
    /**
     * 获取Future持有者
     * @return
     */
    FuturesHolder<Long> getFuturesHolder();

    /**
     * 设置Future持有者
     * @param futuresHolder
     */
    void setFuturesHolder(FuturesHolder<Long> futuresHolder);

    /**
     * 获得服务提供者挑选器
     * @return
     */
    BiFunction<String, Object, Channel> getProviderChooser();

    /**
     * 设置服务提供者挑选器
     * @param providerChooser
     */
    void setProviderChooser(BiFunction<String, Object, Channel> providerChooser);


    /**
     * 获取ID生成器
     * @return
     */
    IdentifierGenerator getIdentifierGenerator();

    /**
     * 设置ID生成器
     * @param identifierGenerator
     */
    void setIdentifierGenerator(IdentifierGenerator identifierGenerator);
}