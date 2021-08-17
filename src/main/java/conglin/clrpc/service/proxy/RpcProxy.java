package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Callback;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;

/**
 * RPC 代理接口
 */
public interface RpcProxy {

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param request 请求
     * @return future
     */
    RpcFuture call(BasicRequest request);

    /**
     * 异步调用函数 指定服务提供者的地址
     *
     * 建议在 {@link Callback#fail(Exception)} 中使用该方法进行重试或回滚 而不应该在一般的调用时使用该方法
     *
     * @param remoteAddress 指定服务提供者的地址
     * @param request       请求
     * @return future
     */
    public RpcFuture callWith(String remoteAddress, BasicRequest request);

}