package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.codec.ProtostuffSerializationHandler;
import conglin.clrpc.common.codec.SerializationHandler;

public class RpcCommonOption {
    
    // 序列化处理器
    private SerializationHandler serializationHandler;

    /**
     * 设置序列化处理器
     * @param serializationHandler the serializationHandler to set
     */
    protected void serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
    }

    /**
     * 获取序列化处理器，若未设置则返回默认值
     * @return the serializationHandler
     */
    public SerializationHandler getSerializationHandler() {
        if(serializationHandler == null) 
            serializationHandler = new ProtostuffSerializationHandler();
        return serializationHandler;
    }
}