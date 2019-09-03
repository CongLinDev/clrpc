package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.service.future.RpcFuture;

public interface TransactionProxy{

    /**
     * 开始一个事务
     * @return
     */
    TransactionProxy begin();

    /**
     * 发送事务内部的一条原子性请求
     * @param serviceName 服务名
     * @param method 服务方法
     * @param args 服务参数
     * @return
     */
    TransactionProxy call(String serviceName, String method, Object... args);

    /**
     * 发送事务内部的一条原子性请求
     * @param serviceName 服务名
     * @param method 服务方法
     * @param args 服务参数
     * @return
     */
    TransactionProxy call(String serviceName, Method method, Object... args);

    /**
     * 提交服务
     * @return
     */
    RpcFuture commit();

    /**
     * 回滚服务
     * @return
     */
    boolean rollback();
}