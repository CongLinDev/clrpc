package conglin.clrpc.service.proxy;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.TransactionRequest;

public interface TransactionProxy {

    /**
     * 开始一个事务，默认非顺序执行
     * 
     * @throws TransactionException
     */
    default void begin() throws TransactionException {
        begin(false);
    }

    /**
     * 开始一个事务
     * 
     * @param serial 是否顺序执行
     * @throws TransactionException
     */
    void begin(boolean serial) throws TransactionException;

    /**
     * 发送事务内部的一条原子性请求
     * 
     * @param serviceName 服务名
     * @param method      服务方法
     * @param args        服务参数
     * @return sub future
     * @throws TransactionException
     */
    RpcFuture call(String serviceName, String method, Object... args) throws TransactionException;

    /**
     * 发送事务内部的一条原子性请求
     * 
     * @param request 请求
     * @return sub future
     * @throws TransactionException
     */
    RpcFuture call(TransactionRequest request) throws TransactionException;

    /**
     * 提交事务
     * 
     * @return transaction future
     * @throws TransactionException
     */
    RpcFuture commit() throws TransactionException;

    /**
     * 中止事务
     * 
     * @throws TransactionException
     */
    void abort() throws TransactionException;

    /**
     * 获取异步原子请求代理
     * 
     * 使用该代理的效果等同于 {@link TransactionProxy#call(String, String, Object...)}
     * 
     * 需要注意的是 该方法产生的对象与 {@code TransactionProxy} 对象深度绑定
     * 
     * @param <T>
     * @param interfaceClass
     * @return
     */
    <T> T subscribeAsync(Class<T> interfaceClass);
}