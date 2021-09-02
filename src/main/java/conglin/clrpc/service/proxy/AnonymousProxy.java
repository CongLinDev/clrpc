package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Callback;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.RequestWrapper;

public class AnonymousProxy implements RpcProxy {
    private final BasicProxy proxy;

    public AnonymousProxy(BasicProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param args        参数
     * @return future
     */
    public RpcFuture call(String serviceName, String methodName, Object... args) {
        return proxy.call(serviceName, methodName, args);
    }

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     *
     * @param remoteAddress 指定远程地址
     * @param serviceName   服务名
     * @param methodName    方法名
     * @param args          参数
     * @return future
     */
    public RpcFuture callWith(String remoteAddress, String serviceName, String methodName, Object... args) {
        return proxy.callWith(remoteAddress, serviceName, methodName, args);
    }

    @Override
    public RpcFuture call(RequestWrapper wrapper) {
        return proxy.call(wrapper);
    }
}
