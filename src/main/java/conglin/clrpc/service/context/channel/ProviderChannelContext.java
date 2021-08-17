package conglin.clrpc.service.context.channel;

import java.util.Map;

import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;

final public class ProviderChannelContext extends CommonChannelContext {

    protected final Map<String, ServiceObject> serviceObjectHolder;

    public ProviderChannelContext(RpcContext context) {
        super(context);
        serviceObjectHolder = context.getWith(RpcContextEnum.SERVICE_OBJECT_HOLDER);
    }

    public Map<String, ServiceObject> getServiceObjectHolder() {
        return serviceObjectHolder;
    }
}