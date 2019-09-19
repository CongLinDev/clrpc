package conglin.clrpc.service.proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.transfer.sender.RequestSender;

abstract public class AbstractProxy {
    protected final String serviceName;
    protected final RequestSender sender;
    protected final IdentifierGenerator identifierGenerator;

    public AbstractProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator){
        this.serviceName = serviceName;
        this.sender = sender;
        this.identifierGenerator = identifierGenerator;
    }

    protected Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }
}