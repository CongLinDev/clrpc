package conglin.clrpc.invocation.proxy;

import conglin.clrpc.invocation.InvocationContext;

/**
 * RPC 代理接口
 */
public interface InvocationProxy {

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param invocationContext 请求
     */
    void call(InvocationContext invocationContext);
}