package conglin.clrpc.service.proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.executor.RequestSender;

abstract public class AbstractProxy {
    // 代理服务名
    protected final String serviceName;
    // 发送器
    protected final RequestSender sender;
    // ID生成器
    protected final IdentifierGenerator identifierGenerator;

    public AbstractProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        this.serviceName = serviceName;
        this.sender = sender;
        this.identifierGenerator = identifierGenerator;
    }

    /**
     * 按顺序返回对象数组所对应的类对象
     * 
     * @param objs
     * @return 类对象数组
     */
    protected Class<?>[] getClassType(Object[] objs) {
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }

    /**
     * 返回关联的服务名
     * @return
     */
    public String getServiceName(){
        return serviceName;
    }
}