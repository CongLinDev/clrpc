package conglin.clrpc.extension.transaction.manager;

import conglin.clrpc.extension.transaction.TransactionException;
import conglin.clrpc.extension.transaction.context.TransactionInvocationContext;
import conglin.clrpc.service.ServiceInterface;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface TransactionManager {

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
     * 需要注意的是 该方法产生的对象与当前 {@link TransactionManager} 对象深度绑定
     * 
     * @param <T>
     * @param serviceInterface
     * @return
     */
    <T> T asyncService(ServiceInterface<T> serviceInterface);
}