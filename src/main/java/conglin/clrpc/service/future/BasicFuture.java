package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transport.message.*;

public class BasicFuture extends AbstractFuture {

    private final RequestPayload request;
    private final Long messageId;
    private ResponsePayload response;

    private boolean fallback; // 是否是 fallback产生的结果，只有在该future已经完成的情况下，该变量才有效

    public BasicFuture(Long messageId, RequestPayload request) {
        super();
        this.request = request;
        this.messageId = messageId;
    }

    @Override
    protected Object doGet() throws RpcServiceException {
        if (response == null)
            return null;
        if (isError()) {
            throw (RpcServiceException) response.result();
        }
        return response.result();
    }

    @Override
    public Long identifier() {
        return messageId;
    }
    
    /**
     * 是否是 Fallback 机制产生的结果
     * 
     * 如果返回值为 {@code true} 则调用 {@link RpcFuture#isDone()} 返回值一定为 {@code true} 
     * 
     * @return
     */
    public final boolean isFallback() {
        return fallback;
    }

    /**
     * 确认该 {@code RpcFuture} 由fallback机制完成
     * 
     * @param response
     */
    public void fallbackDone(ResponsePayload response) {
        fallback = true;
        this.response = response;
        done(response);
    }

    /**
     * 获得与该 RpcFuture 相关联的 RequestPayload
     * 
     * @return
     */
    public final RequestPayload request() {
        return this.request;
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicResponse
     * 
     * @return
     */
    public final ResponsePayload response() {
        return this.response;
    }

    @Override
    protected void beforeDone(Object result) {
        this.response = (ResponsePayload) result;
        if (response.isError())
            signError();
    }

    @Override
    protected void doRunCallback(Callback callback) {
        if (!isError()) {
            callback.success(response.result());
        } else {
            callback.fail((RpcServiceException) response.result());
        }
    }
}