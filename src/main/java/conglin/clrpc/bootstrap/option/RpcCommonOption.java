package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.serialization.ProtostuffSerializationHandler;
import conglin.clrpc.common.serialization.SerializationHandler;

public class RpcCommonOption {

    // 序列化处理器
    private SerializationHandler serializationHandler;

    /**
     * 设置序列化处理器
     * 
     * @param serializationHandler the serializationHandler to set
     * @return this
     */
    protected RpcCommonOption serializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        return this;
    }

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

    private String metaInfo = "";

    /**
     * 设置元信息
     * 
     * @param metaInfo the metaInfo to set
     * @return this
     */
    protected RpcCommonOption metaInfomation(String metaInfo) {
        this.metaInfo = metaInfo;
        return this;
    }

    /**
     * 返回元信息
     * @return the metaInfo
     */
    public String getMetaInfomation() {
        return metaInfo;
    }
}