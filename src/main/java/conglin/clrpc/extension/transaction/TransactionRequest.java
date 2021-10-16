package conglin.clrpc.extension.transaction;

import conglin.clrpc.global.GlobalMessageManager;
import conglin.clrpc.transport.message.BasicRequest;

public class TransactionRequest extends BasicRequest {

    private static final long serialVersionUID = -7860287729080523289L;

    transient public static final int MESSAGE_TYPE = 3;

    static {
        GlobalMessageManager.manager().setMessageClass(MESSAGE_TYPE, TransactionRequest.class);
    }

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    /**
     * 序列号
     */
    protected final Integer serialId;

    /**
     * 构造事务请求
     * 
     * @param transactionId   事务id
     * @param serialId    序列id
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param parameters  参数
     */
    public TransactionRequest(Long transactionId, Integer serialId, String serviceName, String methodName, Object[] parameters) {
        super(transactionId, serviceName, methodName, parameters);
        this.serialId = serialId;
    }

    /**
     * 获取序列ID
     *
     * @return
     */
    final public int serialId() {
        return serialId;
    }

    /**
     * 获取事务ID
     * 
     * @return
     */
    final public long transactionId() {
        return messageId;
    }

    @Override
    public Long messageId() {
        return messageId ^ serialId;
    }
}