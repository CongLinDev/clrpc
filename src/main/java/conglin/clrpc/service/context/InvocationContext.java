package conglin.clrpc.service.context;

import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.transport.message.RequestPayload;

import java.util.function.Consumer;

public class InvocationContext {

    private static final ThreadLocal<InvocationFuture> localFuture = new ThreadLocal<>();

    /**
     * 返回当前线程最新一次操作产生的 future 对象
     * 
     * @return
     */
    public static InvocationFuture lastFuture() {
        return localFuture.get();
    }

    /**
     * 设置当前线程最新一次操作产生的 future 对象
     * 
     * @param future
     */
    public static void lastFuture(InvocationFuture future) {
        localFuture.set(future);
    }

    /**
     * removeFuture
     * 
     * @param future
     */
    public static void removeFuture() {
        localFuture.remove();
    }

    protected RequestPayload request;

    protected Class<? extends FailStrategy> failStrategyClass;

    protected InstanceCondition instanceCondition;

    protected Consumer<ServiceInstance> instanceConsumer;

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        this.request = request;
    }

    /**
     * @return the failStrategyClass
     */
    public Class<? extends FailStrategy> getFailStrategyClass() {
        return failStrategyClass;
    }

    /**
     * @param failStrategyClass the failStrategyClass to set
     */
    public void setFailStrategyClass(Class<? extends FailStrategy> failStrategyClass) {
        this.failStrategyClass = failStrategyClass;
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
}
