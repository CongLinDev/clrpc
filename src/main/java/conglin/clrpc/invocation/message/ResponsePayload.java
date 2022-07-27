package conglin.clrpc.invocation.message;

import java.io.Serial;

import conglin.clrpc.common.ServiceException;

public class ResponsePayload implements Payload {

    @Serial
    private static final long serialVersionUID = 7123186624198529783L;

    transient public static final int PAYLOAD_TYPE = 2;

    private final Object result;
    private boolean error;

    /**
     * 构造一个基本回复对象
     *
     * @param error     是否错误
     * @param result    结果对象
     */
    public ResponsePayload(boolean error, Object result) {
        if (error &&  !(result instanceof ServiceException))
            throw new IllegalArgumentException();
        this.error = error;
        this.result = result;
    }

    /**
     * 构造一个基本回复对象
     *
     * @param result    结果对象
     *
     * @see #ResponsePayload(boolean, Object)
     */
    public ResponsePayload(Object result) {
        this(false, result);
    }

    /**
     * 构造一个基本回复对象
     *
     * @param response
     *
     * @see #ResponsePayload(boolean, Object)
     */
    public ResponsePayload(ResponsePayload response) {
        this(response.isError(), response.result());
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

    @Override
    public String toString() {
        if (isError()) {
            return "ResponsePayload [error=" + result + "]";
        } else {
            return "ResponsePayload [result=" + result + "]";
        }
    }
}
