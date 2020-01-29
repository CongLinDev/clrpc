package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.service.future.RpcFuture;

public interface TransactionProxy {

    /**
     * 开始一个事务，默认非顺序执行
     * 
     * @return this object
     * @throws TransactionException
     */
    TransactionProxy begin() throws TransactionException;

    /**
     * 开始一个事务
     * @param serial 是否顺序执行
     * @return this object
     * @throws TransactionException
     */
    TransactionProxy begin(boolean serial) throws TransactionException;

    /**
     * 发送事务内部的一条原子性请求
     * 
     * @param serviceName 服务名
     * @param method      服务方法
     * @param args        服务参数
     * @return this object
     * @throws TransactionException
     */
    TransactionProxy call(String serviceName, String method, Object... args) throws TransactionException;

    /**
     * 发送事务内部的一条原子性请求
     * 
     * @param serviceName 服务名
     * @param method      服务方法
     * @param args        服务参数
     * @return this object
     * @throws TransactionException
     */
    TransactionProxy call(String serviceName, Method method, Object... args) throws TransactionException;

    /**
     * 提交服务
     * 
     * @return Future
     * @throws TransactionException
     */
    RpcFuture commit() throws TransactionException;

    /**
     * 中止事务
     * 
     * @throws TransactionException
     */
    void abort() throws TransactionException;
}