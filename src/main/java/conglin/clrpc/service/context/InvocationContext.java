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

    // init in constructor
    protected final InvocationFuture future;
    private final long invokeBeginTime;
    private int executeTimes;

    // init in proxy
    protected RequestPayload request;
    protected InstanceCondition choosedInstanceCondition;
    protected Consumer<ServiceInstance> choosedInstancePostProcessor;
    protected FailStrategy failStrategy;
    protected long timeoutThreshold;
    
    // init in exector
    protected Long identifier;

    // init in response
    protected ResponsePayload response;
    private long invokeEndTime;

    public InvocationContext() {
        invokeBeginTime = System.currentTimeMillis();
        future = new BasicFuture();
        executeTimes = 0;
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
     * @return the choosedInstanceCondition
     */
    public InstanceCondition getChoosedInstanceCondition() {
        return choosedInstanceCondition;
    }

    /**
     * @param choosedInstanceCondition the choosedInstanceCondition to set
     */
    public void setChoosedInstanceCondition(InstanceCondition choosedInstanceCondition) {
        this.choosedInstanceCondition = choosedInstanceCondition;
    }

    /**
     * @return the choosedInstancePostProcessor
     */
    public Consumer<ServiceInstance> getChoosedInstancePostProcessor() {
        return choosedInstancePostProcessor;
    }

    /**
     * 
     * @param choosedInstancePostProcessor the choosedInstancePostProcessor to set
     */
    public void setChoosedInstancePostProcessor(Consumer<ServiceInstance> choosedInstancePostProcessor) {
        this.choosedInstancePostProcessor = choosedInstancePostProcessor;
    }

    /**
     * @return the timeoutThreshold
     */
    public long getTimeoutThreshold() {
        return timeoutThreshold;
    }

    /**
     * @param timeoutThreshold the timeoutThreshold to set
     */
    public void setTimeoutThreshold(long timeoutThreshold) {
        this.timeoutThreshold = timeoutThreshold;
    }

    /**
     * 是否超时
     * 
     * @return
     */
    public boolean isTimeout() {
        if (timeoutThreshold <= 0)
            return false;
        return System.currentTimeMillis() > timeoutThreshold + invokeBeginTime;
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

    /**
     * increase executeTimes
     */
    public void increaseExecuteTimes() {
        executeTimes++;
    }

    /**
     * @return the executeTimes
     */
    public int getExecuteTimes() {
        return executeTimes;
    }
}
