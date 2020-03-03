package conglin.clrpc.transport.message;

public class TransactionRequest extends BasicRequest {

    private static final long serialVersionUID = -7860287729080523289L;

    transient public static final int MESSAGE_TYPE = 4;

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    protected Boolean serial;

    /**
     * 构造事务请求
     * 
     * @param messageId 由两部分组成，高32位为事务ID，低32位为序列ID
     * @param serial    是否顺序执行
     */
    public TransactionRequest(Long messageId, Boolean serial) {
        super(messageId);
        this.serial = serial;
    }

    /**
     * 构造事务请求，默认非顺序执行
     * 
     * @param messageId 由两部分组成，高32位为事务ID，低32位为序列ID
     */
    public TransactionRequest(Long messageId) {
        this(messageId, Boolean.FALSE);
    }

    /**
     * 构造事务请求
     * 
     * @param transactionId 事务ID
     * @param serialId      序列ID
     * @param serial        是否顺序执行
     */
    public TransactionRequest(long transactionId, int serialId, Boolean serial) {
        this(transactionId | serialId, serial);
    }

    /**
     * 构造事务请求，默认非顺序执行
     * 
     * @param transactionId 事务ID
     * @param serialId      序列ID
     */
    public TransactionRequest(long transactionId, int serialId) {
        this(transactionId | serialId);
    }

    /**
     * 构造事务请求，深度复制。
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
    public int getSerialId() {
        return getMessageId().intValue();
    }

    /**
     * 获取事务ID
     * 
     * @return
     */
    public long getTransactionId() {
        return getMessageId().longValue() & 0xFFFFFFFF00000000L;
    }

    /**
     * 是否顺序执行
     * 
     * @return the serial
     */
    public Boolean isSerial() {
        return serial;
    }
}