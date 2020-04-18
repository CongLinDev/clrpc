package conglin.clrpc.transport.message;

public class TransactionRequest extends BasicRequest {

    private static final long serialVersionUID = -7860287729080523289L;

    transient public static final int MESSAGE_TYPE = 3;

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    protected boolean serial = false;

    /**
     * 构造事务请求
     * 
     * @param messageId   由两部分组成，高32位为事务ID，低32位为序列ID
     * @param serviceName
     * @param methodName
     * @param parameters
     */
    public TransactionRequest(Long messageId, String serviceName, String methodName, Object[] parameters) {
        super(messageId, serviceName, methodName, parameters);
    }

    /**
     * 构造事务请求
     * 
     * @param transactionId 事务ID
     * @param serialId      序列ID
     * @param serviceName
     * @param methodName
     * @param parameters
     */
    public TransactionRequest(long transactionId, int serialId, String serviceName, String methodName,
            Object[] parameters) {
        this(transactionId | serialId, serviceName, methodName, parameters);
    }

    /**
     * 构造事务请求
     * 
     * @param request
     */
    public TransactionRequest(TransactionRequest request) {
        super(request);
        serial = request.isSerial();
    }

    /**
     * 获取序列ID
     * 
     * @return
     */
    final public int getSerialId() {
        return messageId().intValue();
    }

    /**
     * 获取事务ID
     * 
     * @return
     */
    final public long getTransactionId() {
        return messageId().longValue() & 0xFFFFFFFF00000000L;
    }

    /**
     * 设为顺序执行
     */
    final public void signSerial() {
        serial = true;
    }

    /**
     * 是否顺序执行
     * 
     * @return the serial
     */
    final public boolean isSerial() {
        return serial;
    }
}