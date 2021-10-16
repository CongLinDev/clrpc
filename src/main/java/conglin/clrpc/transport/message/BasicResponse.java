package conglin.clrpc.transport.message;

import conglin.clrpc.global.GlobalMessageManager;

public class BasicResponse extends Message {

    private static final long serialVersionUID = 7123186624198529783L;

    transient public static final int MESSAGE_TYPE = 2;

    static {
        GlobalMessageManager.manager().setMessageClass(MESSAGE_TYPE, BasicResponse.class);
    }

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    private final Object result;
    private boolean error;

    /**
     * 构造一个基本回复对象
     * 
     * @param messageId 消息ID
     * @param error     是否错误
     * @param result    结果对象
     */
    public BasicResponse(Long messageId, boolean error, Object result) {
        super(messageId);
        this.error = error;
        this.result = result;
    }

    /**
     * 构造一个基本回复对象
     * 
     * @param messageId 消息ID
     * @param result    结果对象
     * 
     * @see #BasicResponse(Long, boolean, Object)
     */
    public BasicResponse(Long messageId, Object result) {
        this(messageId, false, result);
    }

    /**
     * 构造一个基本回复对象
     * 
     * @param response
     * 
     * @see #BasicResponse(Long, boolean, Object)
     */
    public BasicResponse(BasicResponse response) {
        this(response.messageId(), response.isError(), response.result());
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
}