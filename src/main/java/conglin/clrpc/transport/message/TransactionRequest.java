package conglin.clrpc.transport.message;

public class TransactionRequest extends BasicRequest {

    transient public static final int MESSAGE_TYPE = 4;

    private static final long serialVersionUID = -7860287729080523289L;

    protected Boolean serial;

    /**
     * requestId 由两部分组成，高32位为事务ID，低32位为序列ID
     * @param requestId
     */
    public TransactionRequest(Long requestId, Boolean serial) {
        super(requestId);
        this.serial = serial;
    }

    /**
     * requestId 由两部分组成，高32位为事务ID，低32位为序列ID
     * @param requestId
     */
    public TransactionRequest(Long requestId) {
        this(requestId, false);
    }

    public TransactionRequest(long transactionId, int serialId, Boolean serial) {
        this(transactionId | serialId, serial);
    }

    public TransactionRequest(long transactionId, int serialId) {
        this(transactionId | serialId);
    }

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
        return getRequestId().intValue();
    }

    public long getTransactionId() {
        return getRequestId().longValue() & 0xFFFFFFFF00000000L;
    }

    /**
     * 是否顺序执行
     * 
     * @return the serial
     */
    public Boolean isSerial() {
        return serial;
    }

    /**
     * @param serial the serial to set
     */
    public void setSerial(Boolean serial) {
        this.serial = serial;
    }
}