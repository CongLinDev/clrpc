package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.serialization.SerializationHandler;

public class RpcProviderOption extends RpcCommonOption {
    /**
     * 设置序列化处理器
     * @param serializationHandler the serializationHandler to set
     * @return this
     */
    public RpcProviderOption setSerializationHandler(SerializationHandler serializationHandler) {
        super.serializationHandler(serializationHandler);
        return this;
    }
}