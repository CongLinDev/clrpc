package conglin.clrpc.bootstrap.option;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.identifier.BasicIdentifierGenerator;
import conglin.clrpc.common.identifier.IdentifierGenerator;

public class RpcConsumerOption extends RpcCommonOption {

    // ID生成器
    private IdentifierGenerator identifierGenerator;

    /**
     * 设置序列化处理器
     * @param serializationHandler the serializationHandler to set
     * @return this
     */
    public RpcConsumerOption setSerializationHandler(SerializationHandler serializationHandler) {
        super.serializationHandler(serializationHandler);
        return this;
    }



    /**
     * 设置 ID生成器
     * @param identifierGenerator the identifierGenerator to set
     * @return this
     */
    public RpcConsumerOption setIdentifierGenerator(IdentifierGenerator identifierGenerator) {
        this.identifierGenerator = identifierGenerator;
        return this;
    }


    /**
     * 获取 ID生成器，若未设置则返回默认值
     * @return the identifierGenerator
     */
    public IdentifierGenerator getIdentifierGenerator() {
        if(identifierGenerator == null)
            identifierGenerator = new BasicIdentifierGenerator();
        return identifierGenerator;
    }
}