package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.identifier.RandomIdentifierGenerator;
import conglin.clrpc.common.serialization.ProtostuffSerializationHandler;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.transport.component.ProviderChooserAdapter;

public class RpcConsumerOption {

    // 序列化处理器
    private SerializationHandler serializationHandler;

    /**
     * 获取序列化处理器，若未设置则返回默认值
     * 
     * @return the serializationHandler
     */
    public SerializationHandler serializationHandler() {
        if (serializationHandler == null)
            serializationHandler = new ProtostuffSerializationHandler();
        return serializationHandler;
    }

    /**
     * 设置序列化处理器
     * 
     * @param serializationHandler the serializationHandler to set
     * @return this
     */
    public RpcConsumerOption serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

    // ID生成器
    private IdentifierGenerator identifierGenerator;

    /**
     * 设置 ID生成器
     * 
     * @param identifierGenerator the identifierGenerator to set
     * @return this
     */
    public RpcConsumerOption identifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
        return this;
    }

    /**
     * 获取 ID生成器，若未设置则返回默认值
     * 
     * @return the identifierGenerator
     */
    public IdentifierGenerator identifierGenerator() {
        if (identifierGenerator == null)
            identifierGenerator = new RandomIdentifierGenerator();
        return identifierGenerator;
    }

    // 服务提供者挑选适配器
    private ProviderChooserAdapter providerChooserAdapter;

    /**
     * 设置 服务提供者挑选适配器
     * 
     * @param providerChooserAdapter the providerChooserAdapter to set
     * @return this
     */
    public RpcConsumerOption providerChooserAdapter(ProviderChooserAdapter providerChooserAdapter) {
        this.providerChooserAdapter = providerChooserAdapter;
        return this;
    }

    /**
     * 获取 服务提供者挑选适配器，若未设置则返回默认值
     * 
     * @return the providerChooserAdapter
     */
    public ProviderChooserAdapter providerChooserAdapter() {
        if (providerChooserAdapter == null)
            providerChooserAdapter = request -> request.methodName().hashCode();
        return providerChooserAdapter;
    }
}