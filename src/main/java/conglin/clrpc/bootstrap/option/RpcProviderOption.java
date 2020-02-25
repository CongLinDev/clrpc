package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.serialization.ProtostuffSerializationHandler;
import conglin.clrpc.common.serialization.SerializationHandler;

public class RpcProviderOption {


    // 序列化处理器
    private SerializationHandler serializationHandler;

    /**
     * 获取序列化处理器，若未设置则返回默认值
     * 
     * @return the serializationHandler
     */
    public SerializationHandler getSerializationHandler() {
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
    public RpcProviderOption setSerializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }
}