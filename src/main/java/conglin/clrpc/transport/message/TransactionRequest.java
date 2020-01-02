package conglin.clrpc.transport.message;

public class TransactionRequest extends BasicRequest {

    transient public static final int MESSAGE_TYPE = 4;

    private static final long serialVersionUID = -7860287729080523289L;

    protected Integer serialNumber;

    public TransactionRequest(Long requestId) {
        super(requestId);
    }

    public TransactionRequest(TransactionRequest request) {
        super(request);
        this.serialNumber = request.getSerialNumber();
    }

    /**
     * 获取序列号
     * 
     * @return the serialNumber of Transaction
     */
    public Integer getSerialNumber() {
        return serialNumber;
    }

    /**
     * 设置序列号
     * 
     * @param serialNumber the serialNumber of Transaction to set
     */
    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }
}