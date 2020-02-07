package conglin.clrpc.transport.message;

import java.io.Serializable;

public class BasicResponse extends Message implements Serializable {

    private static final long serialVersionUID = 7123186624198529783L;

    transient public static final int MESSAGE_TYPE = 1;

    private Object result;
    private Boolean error;

    public BasicResponse(Long requestId) {
        super(requestId);
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
            return "BasicResponse [requestId=" + getRequestId() + ", error=" + result + "]";
        } else {
            return "BasicResponse [requestId=" + getRequestId() + ", result=" + result + "]";
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return new BasicResponse(this);
    }

}