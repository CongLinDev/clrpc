package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.exception.RequestException;
import conglin.clrpc.service.executor.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicFuture extends AbstractFuture {

    protected final BasicRequest request;
    protected BasicResponse response;

    protected final RequestSender sender;

    public BasicFuture(RequestSender sender, BasicRequest request) {
        super();
        this.request = request;
        this.sender = sender;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RequestException {
        try {
            SYNCHRONIZER.acquire(0);
            if (response == null)
                return null;
            if (response.isError()) {
                setError();
                throw (RequestException) response.getResult();
            }
            return response.getResult();
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RequestException {
        try {
            if (SYNCHRONIZER.tryAcquireNanos(0, unit.toNanos(timeout))) {
                if (response == null)
                    return null;
                if (response.isError()) {
                    setError();
                    throw (RequestException) response.getResult();
                }
                return response.getResult();
            } else {
                throw new TimeoutException("Timeout: " + request.toString());
            }
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    @Override
    public void done(Object result) {
        this.response = (BasicResponse) result;
        SYNCHRONIZER.release(0);
        runCallback();
    }

    @Override
    public void retry() {
        SYNCHRONIZER.retry();
        sender.resendRequest(request);
        resetTime();
    }

    @Override
    public long identifier() {
        return request.getRequestId();
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicRequest
     * 
     * @return
     */
    public BasicRequest request() {
        return this.request;
    }

    /**
     * 运行回调函数
     * 
     * @param callback
     */
    @Override
    protected void doRunCallback() {
        if (!isError()) {
            this.futureCallback.success(response.getResult());
        } else {
            this.futureCallback.fail((RequestException) response.getResult());
        }
    }
}