package conglin.clrpc.extension.transaction.payload;

import conglin.clrpc.transport.message.RequestPayload;

import java.io.Serial;

public class TransactionRequestPayload extends RequestPayload {

    @Serial
    private static final long serialVersionUID = -7860287729080523289L;

    transient public static final int PAYLOAD_TYPE = 3;

    /**
     * 事务id
     */
    protected final Long transactionId;
    /**
     * 序列号
     */
    protected final Integer serialId;

    /**
     * 构造事务请求
     *
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param serviceName   服务名
     * @param methodName    方法名
     * @param parameters    参数
     */
    public TransactionRequestPayload(Long transactionId, Integer serialId, String serviceName, String methodName, Object[] parameters) {
        super(serviceName, methodName, parameters);
        this.transactionId = transactionId;
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
        return transactionId;
    }
}
