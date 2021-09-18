package conglin.clrpc.service.proxy;

import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.RequestWrapper;

/**
 * RPC 代理接口
 */
public interface RpcProxy {

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param requestWrapper 请求
     * @return future
     */
    RpcFuture call(RequestWrapper requestWrapper);
}