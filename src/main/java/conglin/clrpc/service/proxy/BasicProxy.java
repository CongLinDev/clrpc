package conglin.clrpc.service.proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;

/**
 * 基本的代理
 * 
 * 适合未知服务名的调用
 */
public class BasicProxy extends CommonProxy {

    // ID生成器
    private final IdentifierGenerator identifierGenerator;

    public BasicProxy(RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender);
        this.identifierGenerator = identifierGenerator;
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
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        return super.call(request);
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
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        return super.callWith(remoteAddress, request);
    }

    /**
     * 标识符生成器
     * 
     * @return
     */
    protected IdentifierGenerator identifierGenerator() {
        return this.identifierGenerator;
    }
}