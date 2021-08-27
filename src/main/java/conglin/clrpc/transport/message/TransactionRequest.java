package conglin.clrpc.transport.message;

public class TransactionRequest extends BasicRequest {

    private static final long serialVersionUID = -7860287729080523289L;

    transient public static final int MESSAGE_TYPE = 3;

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
     * @param messageId   由两部分组成，高32位为事务ID，低32位为序列ID
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param parameters  参数
     */
    public TransactionRequest(Long messageId, Integer serialId, String serviceName, String methodName, Object[] parameters) {
        super(messageId, serviceName, methodName, parameters);
        this.serialId = serialId;
    }

    /**
     * 构造事务请求
     * 
     * @param request
     */
    public TransactionRequest(TransactionRequest request) {
        super(request);
        this.serialId = request.serialId();
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
        return messageId();
    }
}