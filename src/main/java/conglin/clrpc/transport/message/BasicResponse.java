package conglin.clrpc.transport.message;

public class BasicResponse extends Message {

    private static final long serialVersionUID = 7123186624198529783L;

    transient public static final int MESSAGE_TYPE = 2;

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    private Object result;
    private Boolean error;

    public BasicResponse(Long messageId) {
        this(messageId, Boolean.FALSE);
    }

    public BasicResponse(Long messageId, Boolean error) {
        super(messageId);
        this.error = error;
    }

    public BasicResponse(BasicResponse response) {
        super(response);
        this.result = response.getResult();
        this.error = response.isError();
    }

    /**
     * 获得结果
     * 
     * @return the result
     */
    public Object getResult() {
        return result;
    }

    /**
     * 设置结果
     * 
     * @param result the result to set
     */
    public void setResult(Object result) {
        this.result = result;
    }

    /**
     * @return the error
     */
    public Boolean isError() {
        return error;
    }

    /**
     * 设为错误
     */
    public void signError() {
        this.error = Boolean.TRUE;
    }

    @Override
    public String toString() {
        if (isError()) {
            return "BasicResponse [messageId=" + getMessageId() + ", error=" + result + "]";
        } else {
            return "BasicResponse [messageId=" + getMessageId() + ", result=" + result + "]";
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new BasicResponse(this);
    }

}