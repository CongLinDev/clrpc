package conglin.clrpc.service.proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.annotation.AnnotationParser;
import conglin.clrpc.transport.component.RequestSender;

/**
 * 同步对象代理
 * 
 * 代理对象调用方法后 方法阻塞直至返回结果
 */
public class SyncObjectProxy extends BasicProxy {

    private final String serviceName;

    public SyncObjectProxy(Class<?> interfaceClass, RequestSender sender, IdentifierGenerator identifierGenerator) {
        this(AnnotationParser.serviceName(interfaceClass), sender, identifierGenerator);
    }

    public SyncObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender, identifierGenerator);
        this.serviceName = serviceName;
    }

    @Override
    protected String getServiceName(Class<?> methodDeclaringClass) {
        return serviceName;
    }
}