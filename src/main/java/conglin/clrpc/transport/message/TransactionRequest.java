package conglin.clrpc.transport.message;

public class TransactionRequest extends BasicRequest {

    transient public static final int MESSAGE_TYPE = 4;

    private static final long serialVersionUID = -7860287729080523289L;

    protected Integer serialNumber;

    /**
     * @return the serialNumber of Transaction
     */
    public Integer getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the serialNumber of Transaction to set
     */
    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }
}