package conglin.clrpc.service.context;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.service.fallback.FallbackHolder;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.transport.component.ProviderChooser;
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
    FutureHolder<Long> getFuturesHolder();

    /**
     * 设置Future持有者
     * 
     * @param futureHolder
     */
    void setFuturesHolder(FutureHolder<Long> futureHolder);

    /**
     * 获取 fallback object 持有者
     * 
     * @return
     */
    FallbackHolder getFallbackHolder();

    /**
     * 设置 fallback object 持有者
     * 
     * @param fallbackHolder
     */
    void setFallbackHolder(FallbackHolder fallbackHolder);

    /**
     * 获得服务提供者挑选器适配器
     * 
     * @return
     */
    ProviderChooser getProviderChooser();

    /**
     * 设置服务提供者挑选器适配器
     * 
     * @param providerChooser
     */
    void setProviderChooser(ProviderChooser providerChooser);

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

    /**
     * 获得服务注册器
     * 
     * @return
     */
    ServiceRegistry getServiceRegister();

    /**
     * 设置服务注册器
     * 
     * @param serviceRegister
     */
    void setServiceRegister(ServiceRegistry serviceRegister);
}