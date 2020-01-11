package conglin.clrpc.service.context;

import conglin.clrpc.common.identifier.IdentifierGenerator;

import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transport.component.ProviderChooserAdapter;
import conglin.clrpc.transport.component.RequestSender;

public interface ConsumerContext extends CommonContext {
    /**
     * 获取发送器
     * 
     * @return
     */
    RequestSender getRequestSender();

    /**
     * 设置发送器
     * 
     * @param request
     */
    void setRequestSender(RequestSender requestSender);

    /**
     * 获取Future持有者
     * 
     * @return
     */
    FuturesHolder<Long> getFuturesHolder();

    /**
     * 设置Future持有者
     * 
     * @param futuresHolder
     */
    void setFuturesHolder(FuturesHolder<Long> futuresHolder);

    /**
     * 获得服务提供者挑选器适配器
     * 
     * @return
     */
    ProviderChooserAdapter getProviderChooserAdapter();

    /**
     * 设置服务提供者挑选器适配器
     * 
     * @param providerChooser
     */
    void setProviderChooserAdapter(ProviderChooserAdapter providerChooserAdapter);

    /**
     * 获取ID生成器
     * 
     * @return
     */
    IdentifierGenerator getIdentifierGenerator();

    /**
     * 设置ID生成器
     * 
     * @param identifierGenerator
     */
    void setIdentifierGenerator(IdentifierGenerator identifierGenerator);
}