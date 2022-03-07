package conglin.clrpc.extension.transaction;

import conglin.clrpc.service.ServiceInterface;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface TransactionProxy {

    /**
     * 开始一个事务，默认非顺序执行
     * 
     * @throws TransactionException
     */
    void begin() throws TransactionException;

    /**
     * 提交事务
     * 
     * @return transaction context
     * @throws TransactionException
     */
    TransactionInvocationContext commit() throws TransactionException;

    /**
     * 提交事务
     * 
     * @param timeout
     * @param unit
     * @return transaction context
     * @throws TransactionException
     * @throws TimeoutException
     */
    TransactionInvocationContext commit(long timeout, TimeUnit unit) throws TransactionException, TimeoutException;

    /**
     * 中止事务
     * 
     * @return transaction context
     * @throws TransactionException
     */
    TransactionInvocationContext abort() throws TransactionException;

    /**
     * 获取异步原子请求代理
     * 
     * 使用该代理的效果等同于 {@link TransactionProxy#call(String, String, Object...)}
     * 
     * 需要注意的是 该方法产生的对象与 {@code TransactionProxy} 对象深度绑定
     * 
     * @param <T>
     * @param serviceInterface
     * @return
     */
    <T> T proxy(ServiceInterface<T> serviceInterface);
}