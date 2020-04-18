package conglin.clrpc.transport.message;

public class BasicResponse extends Message {

    private static final long serialVersionUID = 7123186624198529783L;

    transient public static final int MESSAGE_TYPE = 2;

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    private final Object result;
    private boolean error;

    public BasicResponse(Long messageId, boolean error, Object result) {
        super(messageId);
        this.error = error;
        this.result = result;
    }

    public BasicResponse(Long messageId, Object result) {
        super(messageId);
        this.result = result;
    }

    public BasicResponse(BasicResponse response) {
        super(response);
        this.result = response.result();
        this.error = response.isError();
    }

    /**
     * 获得结果
     * 
     * @return the result
     */
    final public Object result() {
        return result;
    }

    /**
     * @return the error
     */
    final public boolean isError() {
        return error;
    }

    /**
     * 设为错误
     */
    final public void signError() {
        this.error = true;
    }

    @Override
    public String toString() {
        if (isError()) {
            return "BasicResponse [messageId=" + messageId() + ", error=" + result + "]";
        } else {
            return "BasicResponse [messageId=" + messageId() + ", result=" + result + "]";
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new BasicResponse(this);
    }

}