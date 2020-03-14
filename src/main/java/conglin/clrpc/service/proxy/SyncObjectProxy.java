package conglin.clrpc.service.proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;

/**
 * 同步对象代理
 * 
 * 代理对象调用方法后 方法阻塞直至返回结果
 */
public class SyncObjectProxy extends AbstractObjectProxy {

    public SyncObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(serviceName, sender, identifierGenerator);
    }

    @Override
    protected Object handleFuture(RpcFuture future) throws Exception {
        return future.get();
    }
}