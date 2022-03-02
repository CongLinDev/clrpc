package conglin.clrpc.service.context;

import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailStrategy;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.ResponsePayload;

import java.util.function.Consumer;

public class InvocationContext {

    private static final ThreadLocal<InvocationContext> localContext = new ThreadLocal<>();

    /**
     * 返回当前线程最新一次操作产生的 future 对象
     * 
     * @return
     */
    public static InvocationContext lastContext() {
        return localContext.get();
    }

    /**
     * 设置当前线程最新一次操作产生的 future 对象
     * 
     * @param future
     */
    public static void lastContext(InvocationContext context) {
        localContext.set(context);
    }

    /**
     * removeFuture
     * 
     * @param future
     */
    public static void removeContext() {
        localContext.remove();
    }

    protected final InvocationFuture future;
    private final long invokeBeginTime;
    protected RequestPayload request; // init in proxy
    protected InstanceCondition instanceCondition; // init in proxy
    protected Consumer<ServiceInstance> instanceConsumer; // init in proxy
    protected FailStrategy failStrategy; // init in proxy

    protected Long identifier; // init in sender
    protected ResponsePayload response; // init in response
    private long invokeEndTime;  // init in response

    public InvocationContext() {
        invokeBeginTime = System.currentTimeMillis();
        future = new BasicFuture();
    }

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        if (this.request == null) {
            this.request = request;
        }
    }

    /**
     * @return the instanceCondition
     */
    public InstanceCondition getInstanceCondition() {
        return instanceCondition;
    }

    /**
     * @param instanceCondition the instanceCondition to set
     */
    public void setInstanceCondition(InstanceCondition instanceCondition) {
        this.instanceCondition = instanceCondition;
    }

    public Consumer<ServiceInstance> getInstanceConsumer() {
        return instanceConsumer;
    }

    public void setInstanceConsumer(Consumer<ServiceInstance> instanceConsumer) {
        this.instanceConsumer = instanceConsumer;
    }

    /**
     * @return the response
     */
    public ResponsePayload getResponse() {
        return response;
    }

    /**
     * @param response the response to set
     */
    public void setResponse(ResponsePayload response) {
        if (this.future.isPending()) {
            this.response = response;
            this.invokeEndTime = System.currentTimeMillis();
            this.future.done(response.isError(), response.result());
        }
    }

    /**
     * @return the future
     */
    public InvocationFuture getFuture() {
        return future;
    }

    /**
     * @return the identifier
     */
    public Long getIdentifier() {
        return identifier;
    }

    /**
     * @param identifier the identifier to set
     */
    public void setIdentifier(Long identifier) {
        if (this.identifier == null) {
            this.identifier = identifier;
        }
    }

    /**
     * @return the failStrategy
     */
    public FailStrategy getFailStrategy() {
        return failStrategy;
    }

    /**
     * @param failStrategy the failStrategy to set
     */
    public void setFailStrategy(FailStrategy failStrategy) {
        this.failStrategy = failStrategy;
    }

    /**
     * @return the invokeBeginTime
     */
    public long getInvokeBeginTime() {
        return invokeBeginTime;
    }

    /**
     * @return the invokeEndTime
     */
    public long getInvokeEndTime() {
        return invokeEndTime;
    } 
}
